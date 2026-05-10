package com.openagents.app.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openagents.app.data.api.TaskResponse
import com.openagents.app.di.AppModule
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val repository = AppModule.repository

    val tasks = mutableStateListOf<TaskResponse>()
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    var token = mutableStateOf("")

    fun loadTasks() {
        if (token.value.isBlank()) return
        isLoading.value = true
        error.value = null

        viewModelScope.launch {
            val result = repository.listTasks(token.value)
            result.onSuccess { list ->
                tasks.clear()
                tasks.addAll(list)
            }
            result.onFailure { e ->
                error.value = e.message
            }
            isLoading.value = false
        }
    }

    fun refreshTask(taskId: String) {
        if (token.value.isBlank()) return

        viewModelScope.launch {
            val result = repository.getTask(token.value, taskId)
            result.onSuccess { updated ->
                val index = tasks.indexOfFirst { it.task_id == taskId }
                if (index >= 0) {
                    tasks[index] = updated
                }
            }
        }
    }
}
