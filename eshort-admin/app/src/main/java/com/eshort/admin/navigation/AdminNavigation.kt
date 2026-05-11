package com.eshort.admin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.VideoLibrary
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eshort.admin.ui.screens.dashboard.DashboardScreen
import com.eshort.admin.ui.screens.notifications.NotificationsScreen
import com.eshort.admin.ui.screens.reports.ReportsScreen
import com.eshort.admin.ui.screens.users.UsersScreen
import com.eshort.admin.ui.screens.videos.VideosScreen
import com.eshort.admin.ui.theme.AdminPrimary
import com.eshort.admin.ui.theme.AdminSurface
import com.eshort.admin.ui.theme.AdminTextSecondary

sealed class AdminScreen(val route: String) {
    data object Dashboard : AdminScreen("dashboard")
    data object Users : AdminScreen("users")
    data object Videos : AdminScreen("videos")
    data object Reports : AdminScreen("reports")
    data object Notifications : AdminScreen("notifications")
}

data class AdminNavItem(
    val screen: AdminScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val adminNavItems = listOf(
    AdminNavItem(AdminScreen.Dashboard, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    AdminNavItem(AdminScreen.Users, "Users", Icons.Filled.People, Icons.Outlined.People),
    AdminNavItem(AdminScreen.Videos, "Videos", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    AdminNavItem(AdminScreen.Reports, "Reports", Icons.Filled.Flag, Icons.Outlined.Flag),
    AdminNavItem(AdminScreen.Notifications, "Notify", Icons.Filled.Notifications, Icons.Outlined.Notifications)
)

@Composable
fun AdminNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = AdminSurface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                adminNavItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route == item.screen.route
                    } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label, fontSize = 10.sp) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AdminPrimary,
                            selectedTextColor = AdminPrimary,
                            unselectedIconColor = AdminTextSecondary,
                            unselectedTextColor = AdminTextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AdminScreen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AdminScreen.Dashboard.route) { DashboardScreen() }
            composable(AdminScreen.Users.route) { UsersScreen() }
            composable(AdminScreen.Videos.route) { VideosScreen() }
            composable(AdminScreen.Reports.route) { ReportsScreen() }
            composable(AdminScreen.Notifications.route) { NotificationsScreen() }
        }
    }
}
