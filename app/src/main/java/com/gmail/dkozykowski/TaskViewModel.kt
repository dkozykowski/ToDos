package com.gmail.dkozykowski

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel : ViewModel() {
    val loadTaskLiveData = MutableLiveData<LoadViewState>()
    val sendTaskLiveData = MutableLiveData<SendViewState>()

    fun loadTasks(querryType: QueryTaskType) {
        if (loadTaskLiveData.value == LoadViewState.Loading) return

        loadTaskLiveData.postValue(LoadViewState.Loading)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val tasks = when(querryType) {
                        QueryTaskType.IMPORTANT -> DB.db.taskDao().getImportantActiveTasks()
                        QueryTaskType.ALL_ACTIVE -> DB.db.taskDao().getAllActiveTasks()
                        QueryTaskType.DONE ->  DB.db.taskDao().getDoneTasks()
                    }
                    loadTaskLiveData.postValue(LoadViewState.Success(tasks))
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadTaskLiveData.postValue(LoadViewState.Error(e.message.toString()))
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
}