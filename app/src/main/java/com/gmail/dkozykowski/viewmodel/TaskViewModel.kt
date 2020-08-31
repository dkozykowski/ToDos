package com.gmail.dkozykowski.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.dkozykowski.QueryTaskType
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel : ViewModel() {
    val loadTaskLiveData = MutableLiveData<LoadViewState>()
    val sendTaskLiveData = MutableLiveData<SendViewState>()
    val updateTaskLiveData = MutableLiveData<UpdateViewState>()

    fun loadTasks(queryType: QueryTaskType) {
        if (loadTaskLiveData.value == LoadViewState.Loading) return

        loadTaskLiveData.postValue(LoadViewState.Loading)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val tasks = when (queryType) {
                        QueryTaskType.TODAYS -> DB.db.taskDao().getTodaysActiveTasks()
                        QueryTaskType.ALL_ACTIVE -> DB.db.taskDao().getAllActiveTasks()
                        QueryTaskType.DONE -> DB.db.taskDao().getDoneTasks()
                    }
                    loadTaskLiveData.postValue(
                        LoadViewState.Success(
                            tasks
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadTaskLiveData.postValue(
                        LoadViewState.Error(
                            e.message.toString()
                        )
                    )
                }
            }
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
                    sendTaskLiveData.postValue(
                        SendViewState.Error(
                            e.message.toString()
                        )
                    )
                }
            }
        }
    }

    fun updateTask(id: Int, title: String, description: String, date: Long) {
        if (updateTaskLiveData.value is UpdateViewState.Loading) return

        updateTaskLiveData.postValue(UpdateViewState.Loading)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val taskDao = DB.db.taskDao()
                    var task = taskDao.getTaskById(id)

                    if (task.title != title || task.description != description || task.date != date) {
                        task.title = title
                        task.description = description
                        task.date = date
                        taskDao.updateTask(task)
                    }

                    sendTaskLiveData.postValue(SendViewState.Success)
                } catch (e: Exception) {
                    e.printStackTrace()
                    sendTaskLiveData.postValue(SendViewState.Error(e.message.toString()))
                }
            }
        }
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