package com.gmail.dkozykowski.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.dkozykowski.QueryTaskType
import com.gmail.dkozykowski.QueryTaskType.*
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.data.model.Task
import com.gmail.dkozykowski.model.FilterTaskDataModel
import com.gmail.dkozykowski.model.UpdateTaskDataModel
import com.gmail.dkozykowski.utils.updateTaskWithData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel(private val queryType: QueryTaskType) : ViewModel() {
    val loadTaskLiveData = MutableLiveData<LoadViewState>()
    val sendTaskLiveData = MutableLiveData<SendViewState>()
    val updateTaskLiveData = MutableLiveData<UpdateViewState>()

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
            TODAYS -> DB.db.taskDao().getTodaysActiveTasks()
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
                    val tasks = getFilteredTaskSFromDatabase(filterTaskData)
                    loadTaskLiveData.postValue(LoadViewState.Success(tasks))
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadTaskLiveData.postValue(LoadViewState.Error(e.message.toString()))
                }
            }
        }
    }

    private fun getFilteredTaskSFromDatabase(filterTaskData: FilterTaskDataModel): List<Task> {
        with(filterTaskData) {
            return DB.db.taskDao().getFilteredTasks(title, description, timeLowerBound, timeUpperBound)
        }
    }

    fun sendTask(task: Task) {
        if (sendTaskLiveData.value is SendViewState.Loading) return
        sendTaskLiveData.postValue(SendViewState.Loading)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    DB.db.taskDao().insertTask(task)
                    sendTaskLiveData.postValue(SendViewState.Success)
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
                    updateTaskLiveData.postValue(UpdateViewState.Error(e.message.toString()))
                }
            }
        }
    }

    private fun getTaskFromDatabaseAndUpdate(updateTaskData: UpdateTaskDataModel) {
        val taskToUpdate = DB.db.taskDao().getTaskById(updateTaskData.id)
        taskToUpdate.updateTaskWithData(updateTaskData)
        DB.db.taskDao().updateTask(taskToUpdate)
        updateTaskLiveData.postValue(UpdateViewState.Success)
    }

    sealed class LoadViewState {
        object Loading : LoadViewState()
        class Error(val errorMessage: String) : LoadViewState()
        class Success(val data: List<Task>) : LoadViewState()
    }

    sealed class SendViewState {
        object Loading : SendViewState()
        class Error(val errorMessage: String) : SendViewState()
        object Success : SendViewState()
    }

    sealed class UpdateViewState {
        object Loading : UpdateViewState()
        class Error(val errorMessage: String) : UpdateViewState()
        object Success : UpdateViewState()
    }
}