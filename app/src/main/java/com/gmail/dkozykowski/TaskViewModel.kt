package com.gmail.dkozykowski

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskViewModel: ViewModel() {
    val loadTaskLiveData = MutableLiveData<LoadViewState>()
    //val sendTaskLiveData = MutableLiveData<SendViewState>()

    fun loadTasks() {
        if (loadTaskLiveData.value == LoadViewState.Loading) return

        loadTaskLiveData.postValue(LoadViewState.Loading)

        var data: ArrayList<Task> = ArrayList()
        data.add(Task(0, "lol", "lolek", 0, true, true))
        loadTaskLiveData.postValue(LoadViewState.Success(data))
    }

    sealed class LoadViewState {
        object Loading: LoadViewState()
        class Error(val errorMessage: String): LoadViewState()
        class Success(val data: List<Task>): LoadViewState()
    }
}