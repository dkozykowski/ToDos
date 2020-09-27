package com.gmail.dkozykowski.ui.adapter

import android.content.Context
import android.os.Handler
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dkozykowski.QueryTaskType
import com.gmail.dkozykowski.QueryTaskType.*
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.ui.view.ItemListView
import com.gmail.dkozykowski.utils.createTaskNotification
import com.gmail.dkozykowski.utils.removeTaskNotification
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
    lateinit var context: Context
    private val toast: Toast by lazy {Toast.makeText(context, "", Toast.LENGTH_SHORT)}

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
            { task -> deleteTask(task) },
            { task -> updateTaskFromArgumentSource(task) })
    }

    private fun deleteTask(task: Task) {
        val index = data.indexOfFirst { it.uid == task.uid }
        removeTaskNotification(task, context)
        removeTaskFromDatabase(data[index])
        notifyIdleTaskListUpdated(data[index].done)
        data.removeAt(index)
        displayTaskRemovedPresentation(index)
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
        removeTaskNotification(task, context)
        if (!task.done) createTaskNotification(task, context)
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
            { if(it.date == null) Long.MAX_VALUE else it.date },
            { it.uid }
        ))
    }

    private fun notifyIdleTaskListUpdated(taskMarkedAsDone: Boolean) {
        when (queryType) {
            TODAYS -> notifyIdleTaskListsFromTodaysList()
            ALL_ACTIVE -> notifyIdleTaskListsFromActiveList(taskMarkedAsDone)
            DONE -> notifyIdleTaskListsFromDoneList(taskMarkedAsDone)
            else -> {}
        }
    }

    private fun notifyIdleTaskListsFromTodaysList() {
        updateIdlePageCallback!!(ALL_ACTIVE)
        updateIdlePageCallback!!(DONE)
    }

    private fun notifyIdleTaskListsFromActiveList(taskMarkedAsDone: Boolean) {
        when (taskMarkedAsDone) {
            true -> {
                updateIdlePageCallback!!(TODAYS)
                updateIdlePageCallback!!(DONE)
            }
            false -> updateIdlePageCallback!!(TODAYS)
        }
    }

    private fun notifyIdleTaskListsFromDoneList(taskMarkedAsDone: Boolean) {
        when (taskMarkedAsDone) {
            true -> updateIdlePageCallback!!(TODAYS)
            false -> {
                updateIdlePageCallback!!(TODAYS)
                updateIdlePageCallback!!(ALL_ACTIVE)
            }
        }
    }
}