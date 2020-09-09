package com.gmail.dkozykowski.ui.adapter

import android.os.Handler
import android.view.ViewGroup
import android.widget.Toast
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
    private val showEmptyInfo: () -> Unit,
    private val updateIdlePageCallback: ((QueryTaskType) -> Unit)? = null
) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    class ViewHolder(val postView: ItemListView) : RecyclerView.ViewHolder(postView)
    private var data: ArrayList<Task> = ArrayList()
    lateinit var toast: Toast

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
        holder.postView.bind(
            data[position],
            { deleteTaskAtPosition(position) },
            { task -> updateTaskFromArgumentSource(task) })
    }

    private fun deleteTaskAtPosition(position: Int) {
        removeTaskFromDatabase(data[position])
        data.removeAt(position)
        displayTaskRemovedPresentation(position)
    }

    private fun displayTaskRemovedPresentation(position: Int) {
        if (isDataEmpty()) showEmptyInfo()
        Handler().post { notifyItemRemoved(position) }
    }

    private fun removeTaskFromDatabase(task: Task) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    DB.db.taskDao().deleteTask(task)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateTaskFromArgumentSource(task: Task) {
        val index = data.indexOfFirst { it.uid == task.uid }
        updateTaskInDatabaseWithPagesReloading(task)
        displayTaskChangedPresentation(index, task)
    }

    private fun updateTaskInDatabaseWithPagesReloading(task: Task) {
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
    }

    private fun displayTaskChangedPresentation(index: Int, task: Task) {
        when(queryType) {
            SEARCH -> displayPresentationForSearch(index, task)
            TODAYS -> displayPresentationForTodays(index, task)
            ALL_ACTIVE -> displayPresentationForActive(index, task)
            DONE -> displayPresentationForDone(index, task)
            else -> {}
        }
    }

    private fun displayPresentationForSearch(index: Int, task: Task) {
        displayItemMoved(task, index)
    }

    private fun displayPresentationForActive(index: Int, task: Task) {
        when (task.done) {
            true -> {
                displayItemRemoved(task)
                showToast("Task moved to done")
            }
            false -> displayItemMoved(task, index)
        }
    }

    private fun displayPresentationForDone(index: Int, task: Task) {
        when (task.done) {
            true -> displayItemMoved(task, index)
            false -> {
            displayItemRemoved(task)
            showToast("Task moved to active")
            }
        }
    }

    private fun displayPresentationForTodays(index: Int, task: Task) {
        displayItemMoved(task, index)
    }

    private fun displayItemMoved(task: Task, oldPosition: Int) {
        sortData()
        val newPosition = data.indexOfFirst { it.uid == task.uid }
        Handler().post {
            notifyItemMoved(oldPosition, newPosition)
        }
    }

    private fun displayItemRemoved(task: Task) {
        val position = data.indexOfFirst { it.uid == task.uid }
        data.removeAt(position)
        if (isDataEmpty()) showEmptyInfo()
        Handler().post {
            notifyItemRemoved(position)
        }
    }

    private fun showToast(message: String) {
        toast.setText(message)
        toast.show()
    }

    fun updateData(data: List<Task>) {
        this.data = data as ArrayList<Task>
        sortData()
        Handler().post { notifyDataSetChanged() }
    }

    private fun sortData() {
        data.sortWith(compareBy(
            { it.done },
            { !it.important },
            { it.date },
            { it.uid }
        ))
    }

    private fun notifyIdleTaskListUpdated(taskMarkedAsDone: Boolean) {
        if (queryType == ALL_ACTIVE && !taskMarkedAsDone) {
            updateIdlePageCallback!!(TODAYS)
        } else if (queryType != DONE && taskMarkedAsDone) {
            updateIdlePageCallback!!(DONE)
        } else if (queryType == DONE && !taskMarkedAsDone) {
            updateIdlePageCallback!!(ALL_ACTIVE)
            updateIdlePageCallback!!(TODAYS)
        } else if (queryType == TODAYS && taskMarkedAsDone) {
            updateIdlePageCallback!!(ALL_ACTIVE)
            updateIdlePageCallback!!(DONE)
        } else if (queryType == TODAYS && !taskMarkedAsDone) {
            updateIdlePageCallback!!(ALL_ACTIVE)
        }
    }
}