package io.eshort.user.presentation.navigation

sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Main : Route("main")
    data object Feed : Route("feed")
    data object Explore : Route("explore")
    data object Create : Route("create")
    data object Inbox : Route("inbox")
    data object MyProfile : Route("my_profile")
    data object UserProfile : Route("user_profile/{uid}") {
        fun build(uid: String) = "user_profile/$uid"
    }
    data object EditProfile : Route("edit_profile")
    data object ChatRoom : Route("chat/{conversationId}/{otherUid}") {
        fun build(conversationId: String, otherUid: String) = "chat/$conversationId/$otherUid"
    }
    data object Comments : Route("comments/{videoId}") {
        fun build(videoId: String) = "comments/$videoId"
    }
    data object Settings : Route("settings")
}
