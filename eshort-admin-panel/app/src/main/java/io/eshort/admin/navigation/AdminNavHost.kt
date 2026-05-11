package io.eshort.admin.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Report
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.eshort.admin.ui.screens.BroadcastScreen
import io.eshort.admin.ui.screens.DashboardScreen
import io.eshort.admin.ui.screens.ReportsScreen
import io.eshort.admin.ui.screens.UsersScreen
import io.eshort.admin.ui.screens.VideosScreen
import io.eshort.admin.ui.theme.AdminAsh
import io.eshort.admin.ui.theme.AdminCharcoal
import io.eshort.admin.ui.theme.AdminCoral
import io.eshort.admin.ui.theme.AdminJet
import io.eshort.admin.viewmodel.AdminViewModel

data class AdminTab(
    val route: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

val adminTabs = listOf(
    AdminTab("dashboard", "Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    AdminTab("users", "Users", Icons.Filled.People, Icons.Outlined.People),
    AdminTab("videos", "Videos", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    AdminTab("reports", "Reports", Icons.Filled.Report, Icons.Outlined.Report),
    AdminTab("broadcast", "Notify", Icons.Filled.Notifications, Icons.Outlined.Notifications)
)

@Composable
fun AdminNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination
    val viewModel: AdminViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = AdminCharcoal.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                adminTabs.forEach { tab ->
                    val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
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
                            selectedIconColor = AdminCoral,
                            selectedTextColor = AdminCoral,
                            unselectedIconColor = AdminAsh,
                            unselectedTextColor = AdminAsh,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = AdminJet
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") { DashboardScreen(viewModel) }
            composable("users") { UsersScreen(viewModel) }
            composable("videos") { VideosScreen(viewModel) }
            composable("reports") { ReportsScreen(viewModel) }
            composable("broadcast") { BroadcastScreen(viewModel) }
        }
    }
}
