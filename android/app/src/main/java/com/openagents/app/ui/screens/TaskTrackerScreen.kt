package com.openagents.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openagents.app.data.api.TaskResponse
import com.openagents.app.viewmodel.TaskViewModel

@Composable
fun TaskTrackerScreen(viewModel: TaskViewModel) {
    LaunchedEffect(Unit) { viewModel.loadTasks() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task Tracker",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.loadTasks() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (viewModel.isLoading.value) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        viewModel.error.value?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp),
                fontSize = 12.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.tasks) { task ->
                TaskCard(task = task, onRefresh = { viewModel.refreshTask(task.task_id) })
            }

            if (viewModel.tasks.isEmpty() && !viewModel.isLoading.value) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tasks yet",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: TaskResponse, onRefresh: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (task.status) {
                        "done" -> Icons.Default.CheckCircle
                        "failed" -> Icons.Default.Close
                        "running" -> Icons.Default.PlayArrow
                        else -> Icons.Default.HourglassEmpty
                    },
                    contentDescription = task.status,
                    tint = when (task.status) {
                        "done" -> Color(0xFF4CAF50)
                        "failed" -> Color(0xFFF44336)
                        "running" -> Color(0xFF2196F3)
                        else -> Color(0xFFFFC107)
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.command,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.status == "running" || task.status == "pending") {
                    IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(
                    text = task.status.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (task.status) {
                        "done" -> Color(0xFF4CAF50)
                        "failed" -> Color(0xFFF44336)
                        "running" -> Color(0xFF2196F3)
                        else -> Color(0xFFFFC107)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.created_at.take(19).replace("T", " "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            task.error?.let { err ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = err,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
