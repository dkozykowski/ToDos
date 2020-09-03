package com.gmail.dkozykowski.ui.adapter

import android.content.Context
import android.os.Handler
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dkozykowski.QueryTaskType
import com.gmail.dkozykowski.QueryTaskType.*
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.ui.view.ItemListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskAdapter(
    private val queryType: QueryTaskType,
    context: Context,
    private val showEmptyInfo: () -> Unit,
    private val updateIdlePageCallback: ((QueryTaskType) -> Unit)? = null
) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    class ViewHolder(val postView: ItemListView) : RecyclerView.ViewHolder(postView)

    private var data: ArrayList<Task> = ArrayList()
    private var toast = Toast.makeText(context, "", LENGTH_SHORT)

    fun isDataEmpty(): Boolean {
        return (itemCount == 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListView(parent.context)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.postView.bind(data[position], deleteCallback = {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        DB.db.taskDao().deleteTask(data[position])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            data.removeAt(position)
            if (isDataEmpty()) showEmptyInfo()
            Handler().post { notifyItemRemoved(position) }
        }, updateCallback = { task ->
            val index = data.indexOfFirst { it.uid == task.uid }

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        DB.db.taskDao().updateTask(task)
                        notifyIdleTaskListUpdated(task.done)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            displayTaskChangedPresentation(index, task)
        })
    }

    private fun displayTaskChangedPresentation(index: Int, task: Task) {
        if ((queryType == ALL_ACTIVE && !task.done)
            || (queryType == DONE && task.done)
            || (queryType == TODAYS && !task.done)) {
            sortData()
            Handler().post {
                notifyItemMoved(index, data.indexOfFirst { it.uid == task.uid })
            }
        } else if ((queryType != DONE && task.done) || (queryType == DONE && !task.done)) {
            val position = data.indexOfFirst { it.uid == task.uid }
            data.removeAt(position)
            if (isDataEmpty()) showEmptyInfo()
            Handler().post { notifyItemRemoved(position) }
            toast.setText(
                if (task.done) "Task moved to done"
                else "Task moved to active"
            )
            toast.show()
        }
    }

    fun updateData(data: List<Task>) {
        this.data = data as ArrayList<Task>
        sortData()
        Handler().post { notifyDataSetChanged() }
    }

    private fun sortData() {
        data.sortWith(compareBy(
            { !it.important },
            { it.date },
            { it.uid }
        ))
    }

    private fun notifyIdleTaskListUpdated(taskParamDone: Boolean) {
        if ((queryType == ALL_ACTIVE && !taskParamDone) || (queryType == DONE && taskParamDone)) {
            updateIdlePageCallback!!(TODAYS)
        } else if (queryType != DONE && taskParamDone) {
            updateIdlePageCallback!!(DONE)
        } else if (queryType == DONE && !taskParamDone) {
            updateIdlePageCallback!!(DONE)
            updateIdlePageCallback!!(ALL_ACTIVE)
        } else if (queryType == TODAYS && taskParamDone) {
            updateIdlePageCallback!!(ALL_ACTIVE)
            updateIdlePageCallback!!(DONE)
        } else if (queryType == TODAYS && !taskParamDone) {
            updateIdlePageCallback!!(ALL_ACTIVE)
        }
    }
}