package com.openagents.app.data.api

import retrofit2.Response
import retrofit2.http.*

// --- Request/Response models ---

data class RegisterRequest(val email: String, val username: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(
    val user_id: String,
    val email: String,
    val username: String,
    val access_token: String,
    val token_type: String
)

data class TaskRequest(val command: String)
data class TaskResponse(
    val task_id: String,
    val command: String,
    val status: String,
    val result: String?,
    val steps: String?,
    val error: String?,
    val created_at: String,
    val updated_at: String
)

data class ChatRequest(val message: String)
data class ChatResponse(val response: String, val planned_steps: List<StepInfo>?)
data class StepInfo(val action: String, val params: Map<String, Any>?, val description: String?)

data class NavigateRequest(val url: String)
data class BrowserResult(val status: String, val url: String?, val title: String?, val error: String?)
data class ScreenshotResult(val status: String, val path: String?, val base64: String?, val error: String?)
data class TabInfo(val id: String, val url: String, val title: String, val active: Boolean)
data class TabListResult(val status: String, val tabs: List<TabInfo>?)

data class FileInfo(
    val name: String,
    val path: String,
    val is_dir: Boolean,
    val size: Long,
    val modified: Double
)
data class FileListResult(val files: List<FileInfo>)
data class WorkspaceInfo(val workspace_path: String, val total_size_bytes: Long, val total_size_mb: Double)

data class UserResponse(val user_id: String, val email: String, val username: String, val is_active: Boolean)

// --- API Interface ---

interface OpenAgentsApi {
    // Auth
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/v1/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserResponse>

    // Tasks
    @POST("api/v1/tasks/execute")
    suspend fun executeTask(
        @Header("Authorization") token: String,
        @Body request: TaskRequest
    ): Response<TaskResponse>

    @POST("api/v1/tasks/submit")
    suspend fun submitTask(
        @Header("Authorization") token: String,
        @Body request: TaskRequest
    ): Response<TaskResponse>

    @GET("api/v1/tasks/{taskId}")
    suspend fun getTask(
        @Header("Authorization") token: String,
        @Path("taskId") taskId: String
    ): Response<TaskResponse>

    @GET("api/v1/tasks/")
    suspend fun listTasks(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<TaskResponse>>

    // Agent
    @POST("api/v1/agent/chat")
    suspend fun chat(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>

    // Browser
    @POST("api/v1/browser/navigate")
    suspend fun navigate(
        @Header("Authorization") token: String,
        @Body request: NavigateRequest
    ): Response<BrowserResult>

    @POST("api/v1/browser/screenshot")
    suspend fun screenshot(
        @Header("Authorization") token: String
    ): Response<ScreenshotResult>

    @GET("api/v1/browser/tabs")
    suspend fun listTabs(
        @Header("Authorization") token: String
    ): Response<TabListResult>

    // Files
    @GET("api/v1/files/list")
    suspend fun listFiles(
        @Header("Authorization") token: String,
        @Query("subdir") subdir: String = ""
    ): Response<FileListResult>

    @GET("api/v1/files/workspace")
    suspend fun getWorkspaceInfo(
        @Header("Authorization") token: String
    ): Response<WorkspaceInfo>
}
