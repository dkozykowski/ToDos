package com.gmail.dkozykowski

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

    fun loadTasks() {
        if (loadTaskLiveData.value == LoadViewState.Loading) return

        loadTaskLiveData.postValue(LoadViewState.Loading)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val tasks = DB.db.taskDao().getAllActiveTasks()
                    loadTaskLiveData.postValue(LoadViewState.Success(tasks))
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadTaskLiveData.postValue(LoadViewState.Error(e.message.toString()))
                }
            }
        }
    }

    fun sendTask(task: Task) {
        if (sendTaskLiveData.value == SendViewState.Loading) return

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