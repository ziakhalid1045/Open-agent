package com.openagents.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openagents.app.ui.screens.*
import com.openagents.app.ui.theme.OpenAgentsTheme
import com.openagents.app.viewmodel.ChatViewModel
import com.openagents.app.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenAgentsTheme {
                OpenAgentsApp()
            }
        }
    }
}

@Composable
fun OpenAgentsApp() {
    var token by remember { mutableStateOf<String?>(null) }

    if (token == null) {
        LoginScreen(onLoginSuccess = { token = it })
    } else {
        MainScreen(token = token!!)
    }
}

@Composable
fun MainScreen(token: String) {
    val chatViewModel: ChatViewModel = viewModel()
    val taskViewModel: TaskViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }

    chatViewModel.token.value = token
    taskViewModel.token.value = token

    val tabs = listOf(
        "Chat" to Icons.Default.Chat,
        "Browser" to Icons.Default.Language,
        "Files" to Icons.Default.Folder,
        "Tasks" to Icons.Default.List
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, (title, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> ChatScreen(viewModel = chatViewModel)
                1 -> BrowserScreen(token = token)
                2 -> FileManagerScreen(token = token)
                3 -> TaskTrackerScreen(viewModel = taskViewModel)
            }
        }
    }
}
