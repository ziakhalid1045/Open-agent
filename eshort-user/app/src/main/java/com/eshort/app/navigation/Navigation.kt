package com.eshort.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoCall
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eshort.app.ui.screens.auth.LoginScreen
import com.eshort.app.ui.screens.chat.ChatDetailScreen
import com.eshort.app.ui.screens.chat.ChatListScreen
import com.eshort.app.ui.screens.comments.CommentsSheet
import com.eshort.app.ui.screens.home.HomeFeedScreen
import com.eshort.app.ui.screens.profile.EditProfileScreen
import com.eshort.app.ui.screens.profile.ProfileScreen
import com.eshort.app.ui.screens.search.SearchScreen
import com.eshort.app.ui.screens.splash.SplashScreen
import com.eshort.app.ui.screens.upload.UploadScreen
import com.eshort.app.ui.theme.EShortDarkSurface
import com.eshort.app.ui.theme.EShortMediumGray
import com.eshort.app.ui.theme.EShortPrimary
import com.eshort.app.ui.theme.EShortWhite
import com.eshort.app.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Upload : Screen("upload")
    data object ChatList : Screen("chat_list")
    data object ChatDetail : Screen("chat/{chatRoomId}/{otherUserId}") {
        fun createRoute(chatRoomId: String, otherUserId: String) = "chat/$chatRoomId/$otherUserId"
    }
    data object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    data object EditProfile : Screen("edit_profile")
    data object Comments : Screen("comments/{videoId}") {
        fun createRoute(videoId: String) = "comments/$videoId"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Search, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem(Screen.Upload, "Upload", Icons.Filled.VideoCall, Icons.Outlined.VideoCall),
    BottomNavItem(Screen.ChatList, "Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.PersonOutline)
)

@Composable
fun EShortNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                isLoggedIn = authState.isLoggedIn
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            MainScaffold(navController = navController, currentRoute = Screen.Home.route) {
                HomeFeedScreen(
                    onCommentClick = { videoId ->
                        navController.navigate(Screen.Comments.createRoute(videoId))
                    },
                    onProfileClick = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            }
        }

        composable(Screen.Search.route) {
            MainScaffold(navController = navController, currentRoute = Screen.Search.route) {
                SearchScreen(
                    onVideoClick = { },
                    onUserClick = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            }
        }

        composable(Screen.Upload.route) {
            MainScaffold(navController = navController, currentRoute = Screen.Upload.route) {
                UploadScreen(
                    onUploadSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.ChatList.route) {
            MainScaffold(navController = navController, currentRoute = Screen.ChatList.route) {
                ChatListScreen(
                    onChatClick = { chatRoomId, otherUserId ->
                        navController.navigate(Screen.ChatDetail.createRoute(chatRoomId, otherUserId))
                    }
                )
            }
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("chatRoomId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType }
            )
        ) { entry ->
            val chatRoomId = entry.arguments?.getString("chatRoomId") ?: ""
            val otherUserId = entry.arguments?.getString("otherUserId") ?: ""
            ChatDetailScreen(
                chatRoomId = chatRoomId,
                otherUserId = otherUserId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { entry ->
            val userId = entry.arguments?.getString("userId") ?: ""
            val displayUserId = if (userId == "me") authState.user?.uid ?: "" else userId
            MainScaffold(navController = navController, currentRoute = Screen.Profile.route) {
                ProfileScreen(
                    userId = displayUserId,
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onChatClick = { otherUserId ->
                        // Start chat and navigate
                    },
                    onBack = { navController.popBackStack() },
                    onVideoClick = { }
                )
            }
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Comments.route,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { entry ->
            val videoId = entry.arguments?.getString("videoId") ?: ""
            CommentsSheet(
                videoId = videoId,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = EShortDarkSurface.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    val route = when (item.screen) {
                        Screen.Profile -> Screen.Profile.createRoute("me")
                        else -> item.screen.route
                    }
                    val isSelected = when (item.screen) {
                        Screen.Profile -> currentDestination?.route?.startsWith("profile") == true
                        else -> currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                    }

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EShortPrimary,
                            selectedTextColor = EShortPrimary,
                            unselectedIconColor = EShortMediumGray,
                            unselectedTextColor = EShortMediumGray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(padding)
        ) {
            content()
        }
    }
}
