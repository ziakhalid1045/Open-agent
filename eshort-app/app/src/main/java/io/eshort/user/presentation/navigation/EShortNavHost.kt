package io.eshort.user.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.eshort.user.core.auth.AuthState
import io.eshort.user.core.auth.AuthViewModel
import io.eshort.user.presentation.screens.comments.CommentsScreen
import io.eshort.user.presentation.screens.create.CreateScreen
import io.eshort.user.presentation.screens.explore.ExploreScreen
import io.eshort.user.presentation.screens.feed.FeedScreen
import io.eshort.user.presentation.screens.inbox.InboxScreen
import io.eshort.user.presentation.screens.inbox.ChatRoomScreen
import io.eshort.user.presentation.screens.login.LoginScreen
import io.eshort.user.presentation.screens.profile.ProfileScreen
import io.eshort.user.presentation.screens.profile.EditProfileScreen
import io.eshort.user.presentation.screens.settings.SettingsScreen
import io.eshort.user.presentation.screens.splash.SplashScreen
import io.eshort.user.presentation.theme.Charcoal
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Ash
import io.eshort.user.presentation.theme.Jet

data class NavTab(
    val route: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

val mainTabs = listOf(
    NavTab(Route.Feed.path, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavTab(Route.Explore.path, "Explore", Icons.Filled.Search, Icons.Outlined.Search),
    NavTab(Route.Create.path, "Create", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    NavTab(Route.Inbox.path, "Inbox", Icons.Filled.MailOutline, Icons.Outlined.MailOutline),
    NavTab(Route.MyProfile.path, "Me", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun EShortNavHost(
    authState: AuthState,
    authViewModel: AuthViewModel
) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Route.Splash.path,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        composable(Route.Splash.path) {
            SplashScreen(
                onFinished = {
                    val dest = if (authState.user != null) Route.Main.path else Route.Login.path
                    rootNavController.navigate(dest) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Login.path) {
            LoginScreen(
                authViewModel = authViewModel,
                onSignedIn = {
                    rootNavController.navigate(Route.Main.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Main.path) {
            MainShell(
                authViewModel = authViewModel,
                rootNavController = rootNavController
            )
        }

        composable(
            Route.UserProfile.path,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { entry ->
            val uid = entry.arguments?.getString("uid").orEmpty()
            ProfileScreen(
                uid = uid,
                isSelf = false,
                onBack = { rootNavController.popBackStack() },
                onNavigateToChat = { convoId, otherUid ->
                    rootNavController.navigate(Route.ChatRoom.build(convoId, otherUid))
                }
            )
        }

        composable(Route.EditProfile.path) {
            EditProfileScreen(onBack = { rootNavController.popBackStack() })
        }

        composable(
            Route.ChatRoom.path,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("otherUid") { type = NavType.StringType }
            )
        ) { entry ->
            val convoId = entry.arguments?.getString("conversationId").orEmpty()
            val otherUid = entry.arguments?.getString("otherUid").orEmpty()
            ChatRoomScreen(
                conversationId = convoId,
                otherUid = otherUid,
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable(
            Route.Comments.path,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { entry ->
            val videoId = entry.arguments?.getString("videoId").orEmpty()
            CommentsScreen(
                videoId = videoId,
                onBack = { rootNavController.popBackStack() }
            )
        }

        composable(Route.Settings.path) {
            SettingsScreen(
                authViewModel = authViewModel,
                onBack = { rootNavController.popBackStack() },
                onLoggedOut = {
                    rootNavController.navigate(Route.Login.path) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainShell(
    authViewModel: AuthViewModel,
    rootNavController: androidx.navigation.NavHostController
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Charcoal.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                mainTabs.forEach { tab ->
                    val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Coral,
                            selectedTextColor = Coral,
                            unselectedIconColor = Ash,
                            unselectedTextColor = Ash,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = Jet
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = Route.Feed.path,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.Feed.path) {
                FeedScreen(
                    onComments = { videoId ->
                        rootNavController.navigate(Route.Comments.build(videoId))
                    },
                    onProfile = { uid ->
                        rootNavController.navigate(Route.UserProfile.build(uid))
                    }
                )
            }
            composable(Route.Explore.path) {
                ExploreScreen(
                    onUserClick = { uid ->
                        rootNavController.navigate(Route.UserProfile.build(uid))
                    }
                )
            }
            composable(Route.Create.path) {
                CreateScreen()
            }
            composable(Route.Inbox.path) {
                InboxScreen(
                    onConversationClick = { convoId, otherUid ->
                        rootNavController.navigate(Route.ChatRoom.build(convoId, otherUid))
                    }
                )
            }
            composable(Route.MyProfile.path) {
                ProfileScreen(
                    uid = authViewModel.authState.value.user?.uid.orEmpty(),
                    isSelf = true,
                    onBack = {},
                    onNavigateToChat = { convoId, otherUid ->
                        rootNavController.navigate(Route.ChatRoom.build(convoId, otherUid))
                    },
                    onEditProfile = {
                        rootNavController.navigate(Route.EditProfile.path)
                    },
                    onSettings = {
                        rootNavController.navigate(Route.Settings.path)
                    }
                )
            }
        }
    }
}
