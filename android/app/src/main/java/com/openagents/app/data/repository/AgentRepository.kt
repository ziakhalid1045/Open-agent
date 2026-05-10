package com.openagents.app.data.repository

import com.openagents.app.data.api.*
import retrofit2.Response

class AgentRepository(private val api: OpenAgentsApi) {

    suspend fun register(email: String, username: String, password: String): Result<AuthResponse> {
        return safeCall { api.register(RegisterRequest(email, username, password)) }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return safeCall { api.login(LoginRequest(email, password)) }
    }

    suspend fun getMe(token: String): Result<UserResponse> {
        return safeCall { api.getMe("Bearer $token") }
    }

    suspend fun executeTask(token: String, command: String): Result<TaskResponse> {
        return safeCall { api.executeTask("Bearer $token", TaskRequest(command)) }
    }

    suspend fun submitTask(token: String, command: String): Result<TaskResponse> {
        return safeCall { api.submitTask("Bearer $token", TaskRequest(command)) }
    }

    suspend fun getTask(token: String, taskId: String): Result<TaskResponse> {
        return safeCall { api.getTask("Bearer $token", taskId) }
    }

    suspend fun listTasks(token: String, limit: Int = 20, offset: Int = 0): Result<List<TaskResponse>> {
        return safeCall { api.listTasks("Bearer $token", limit, offset) }
    }

    suspend fun chat(token: String, message: String): Result<ChatResponse> {
        return safeCall { api.chat("Bearer $token", ChatRequest(message)) }
    }

    suspend fun navigate(token: String, url: String): Result<BrowserResult> {
        return safeCall { api.navigate("Bearer $token", NavigateRequest(url)) }
    }

    suspend fun screenshot(token: String): Result<ScreenshotResult> {
        return safeCall { api.screenshot("Bearer $token") }
    }

    suspend fun listTabs(token: String): Result<TabListResult> {
        return safeCall { api.listTabs("Bearer $token") }
    }

    suspend fun listFiles(token: String, subdir: String = ""): Result<FileListResult> {
        return safeCall { api.listFiles("Bearer $token", subdir) }
    }

    suspend fun getWorkspaceInfo(token: String): Result<WorkspaceInfo> {
        return safeCall { api.getWorkspaceInfo("Bearer $token") }
    }

    private suspend fun <T> safeCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
