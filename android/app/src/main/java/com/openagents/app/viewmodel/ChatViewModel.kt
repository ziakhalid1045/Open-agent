package com.openagents.app.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openagents.app.di.AppModule
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val steps: List<Map<String, Any?>>? = null
)

class ChatViewModel : ViewModel() {
    private val repository = AppModule.repository

    val messages = mutableStateListOf<ChatMessage>()
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    var token = mutableStateOf("")

    fun sendMessage(text: String) {
        if (text.isBlank() || token.value.isBlank()) return

        messages.add(ChatMessage(text = text, isUser = true))
        isLoading.value = true
        error.value = null

        viewModelScope.launch {
            // First chat with agent to get plan
            val chatResult = repository.chat(token.value, text)
            chatResult.onSuccess { response ->
                messages.add(
                    ChatMessage(
                        text = response.response,
                        isUser = false,
                        steps = response.planned_steps?.map { step ->
                            mapOf(
                                "action" to step.action,
                                "description" to step.description
                            )
                        }
                    )
                )
            }

            // Then execute the task
            val taskResult = repository.submitTask(token.value, text)
            taskResult.onSuccess { task ->
                messages.add(
                    ChatMessage(
                        text = "Task submitted (${task.task_id}). Status: ${task.status}",
                        isUser = false
                    )
                )
            }
            taskResult.onFailure { e ->
                error.value = e.message
            }

            isLoading.value = false
        }
    }

    fun executeDirectCommand(command: String) {
        if (command.isBlank() || token.value.isBlank()) return

        messages.add(ChatMessage(text = "[Execute] $command", isUser = true))
        isLoading.value = true

        viewModelScope.launch {
            val result = repository.executeTask(token.value, command)
            result.onSuccess { task ->
                messages.add(
                    ChatMessage(
                        text = "Result: ${task.result ?: task.status}",
                        isUser = false
                    )
                )
            }
            result.onFailure { e ->
                messages.add(
                    ChatMessage(text = "Error: ${e.message}", isUser = false)
                )
            }
            isLoading.value = false
        }
    }
}
