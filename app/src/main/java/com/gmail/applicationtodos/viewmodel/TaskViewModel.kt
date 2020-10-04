package com.gmail.applicationtodos.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.applicationtodos.QueryTaskType
import com.gmail.applicationtodos.QueryTaskType.*
import com.gmail.applicationtodos.data.DB
import com.gmail.applicationtodos.data.model.Task
import com.gmail.applicationtodos.model.FilterTaskDataModel
import com.gmail.applicationtodos.model.UpdateTaskDataModel
import com.gmail.applicationtodos.utils.createTaskNotificationPendingEvent
import com.gmail.applicationtodos.utils.removeTaskNotificationPendingEvent
import com.gmail.applicationtodos.utils.updateTaskWithData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel(private val queryType: QueryTaskType) : ViewModel() {
    val loadTaskLiveData = MutableLiveData<LoadViewState>()
    val sendTaskLiveData = MutableLiveData<SendViewState>()
    val updateTaskLiveData = MutableLiveData<UpdateViewState>()
    lateinit var context: Context

    fun loadTasksWithoutFilters() {
        if (loadTaskLiveData.value == LoadViewState.Loading) return
        loadTaskLiveData.postValue(LoadViewState.Loading)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    loadTaskLiveData.postValue(LoadViewState.Success(getTaskFromDatabase()))
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadTaskLiveData.postValue(LoadViewState.Error(e.message.toString()))
                }
            }
        }
    }

    private fun getTaskFromDatabase(): List<Task> {
        return when (queryType) {
            TODAYS -> DB.db.taskDao().getTodaysTasks()
            ALL_ACTIVE -> DB.db.taskDao().getAllActiveTasks()
            DONE -> DB.db.taskDao().getDoneTasks()
            else -> ArrayList<Task>()
        }
    }

    fun loadTasksWithFilters(filterTaskData: FilterTaskDataModel) {
        if (loadTaskLiveData.value == LoadViewState.Loading) return
        loadTaskLiveData.postValue(LoadViewState.Loading)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val tasks = getFilteredTasksFromDatabase(filterTaskData)
                    loadTaskLiveData.postValue(LoadViewState.Success(tasks))
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadTaskLiveData.postValue(LoadViewState.Error(e.message.toString()))
                }
            }
        }
    }

    private fun getFilteredTasksFromDatabase(filterTaskData: FilterTaskDataModel): List<Task> {
        with(filterTaskData) {
            return if (timeLowerBound == 0L && timeUpperBound == Long.MAX_VALUE) DB.db.taskDao()
                .getFilteredTasksWithoutDate(title, description) else DB.db.taskDao()
                .getFilteredTasksWithDate(title, description, timeLowerBound, timeUpperBound)
        }
    }

    fun sendTask(task: Task) {
        if (sendTaskLiveData.value is SendViewState.Loading) return
        sendTaskLiveData.postValue(SendViewState.Loading)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val sentTaskDatabaseId = DB.db.taskDao().insertTask(task)
                    task.uid = sentTaskDatabaseId
                    sendTaskLiveData.postValue(SendViewState.Success(task))
                } catch (e: Exception) {
                    e.printStackTrace()
                    sendTaskLiveData.postValue(SendViewState.Error(e.message.toString()))
                }
            }
        }
    }

    fun updateTask(updateTaskData: UpdateTaskDataModel) {
        if (updateTaskLiveData.value is UpdateViewState.Loading) return
        updateTaskLiveData.postValue(UpdateViewState.Loading)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    getTaskFromDatabaseAndUpdate(updateTaskData)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getTaskFromDatabaseAndUpdate(updateTaskData: UpdateTaskDataModel) {
        val taskToUpdate = DB.db.taskDao().getTaskById(updateTaskData.id)
        removeTaskNotificationPendingEvent(taskToUpdate, context)
        taskToUpdate.updateTaskWithData(updateTaskData)
        createTaskNotificationPendingEvent(taskToUpdate, context)
        DB.db.taskDao().updateTask(taskToUpdate)
        updateTaskLiveData.postValue(UpdateViewState.Success(taskToUpdate))
    }

    sealed class LoadViewState {
        object Loading : LoadViewState()
        class Error(val errorMessage: String) : LoadViewState()
        class Success(val data: List<Task>) : LoadViewState()
    }

    sealed class SendViewState {
        object Loading : SendViewState()
        class Error(val errorMessage: String) : SendViewState()
        class Success(val task: Task) : SendViewState()
    }

    sealed class UpdateViewState {
        class Success(val task: Task) : UpdateViewState()
        class Error(val errorMessage: String) : UpdateViewState()
        object Loading : UpdateViewState()
    }
}