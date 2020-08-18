package com.gmail.dkozykowski.ui.adapter

import android.os.Handler
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dkozykowski.QueryTaskType
import com.gmail.dkozykowski.QueryTaskType.*
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.utils.minimum
import com.gmail.dkozykowski.ui.view.ItemListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskAdapter(private val queryType: QueryTaskType) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    class ViewHolder(val postView: ItemListView) : RecyclerView.ViewHolder(postView)

    private var data: ArrayList<Task> = ArrayList()

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
            removeTaskAnimation(position)
        }, updateCallback = { task, context ->
            val index = data.indexOfFirst { it.uid == task.uid }

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        DB.db.taskDao().updateTask(task)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if ((queryType == ALL_ACTIVE && !task.done) || (queryType == DONE && task.done)) {
                sortData()
                moveTaskAnimation(index, data.indexOfFirst { it.uid == task.uid })
            } else if ((queryType != DONE && task.done) || (queryType == DONE && !task.done)) {
                val position = data.indexOfFirst { it.uid == task.uid }
                data.removeAt(position)
                removeTaskAnimation(position)
                Toast.makeText(
                    context, if (task.done) "Task moved to done"
                    else "Task moved to active", Toast.LENGTH_SHORT
                ).show()
            } else if (queryType == IMPORTANT && !task.important) {
                data.removeAt(index)
                removeTaskAnimation(index)
            }
        })
    }

    private fun removeTaskAnimation(position: Int) {
        Handler().post {
            notifyItemRemoved(position)
        }
    }

    private fun moveTaskAnimation(oldPosition: Int, newPosition: Int) {
        Handler().post {
            notifyItemMoved(oldPosition, newPosition)
        }
    }

    fun updateData(data: List<Task>) {
        this.data = data as ArrayList<Task>
        sortData()
        Handler().post {
            notifyDataSetChanged()
        }
    }

    private fun sortData() {
        data.sortWith(compareBy(
            { !it.important },
            { it.date },
            { it.uid }
        ))
    }
}