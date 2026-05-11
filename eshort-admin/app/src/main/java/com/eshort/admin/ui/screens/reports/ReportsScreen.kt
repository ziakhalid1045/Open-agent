package com.eshort.admin.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eshort.admin.data.model.AdminReport
import com.eshort.admin.ui.theme.AdminCard
import com.eshort.admin.ui.theme.AdminDanger
import com.eshort.admin.ui.theme.AdminSuccess
import com.eshort.admin.ui.theme.AdminTextSecondary
import com.eshort.admin.ui.theme.AdminWarning
import com.eshort.admin.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("Reports", fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "${state.reports.count { it.status == "pending" }} pending",
                        color = AdminWarning,
                        fontSize = 13.sp
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (state.reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Flag,
                        null,
                        tint = AdminTextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No reports", color = Color.White, fontSize = 18.sp)
                    Text("All clear!", color = AdminTextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.reports) { report ->
                    ReportCard(
                        report = report,
                        onResolve = { viewModel.resolveReport(report.id, "resolved") },
                        onDismiss = { viewModel.resolveReport(report.id, "dismissed") }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun ReportCard(
    report: AdminReport,
    onResolve: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AdminCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Flag,
                null,
                tint = when (report.status) {
                    "pending" -> AdminWarning
                    "resolved" -> AdminSuccess
                    else -> AdminTextSecondary
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        report.targetType.replaceFirstChar { it.uppercase() },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                when (report.status) {
                                    "pending" -> AdminWarning.copy(alpha = 0.2f)
                                    "resolved" -> AdminSuccess.copy(alpha = 0.2f)
                                    else -> AdminTextSecondary.copy(alpha = 0.2f)
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            report.status.replaceFirstChar { it.uppercase() },
                            color = when (report.status) {
                                "pending" -> AdminWarning
                                "resolved" -> AdminSuccess
                                else -> AdminTextSecondary
                            },
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(report.reason, color = AdminTextSecondary, fontSize = 13.sp)
                Text(
                    "By: ${report.reporterName}",
                    color = AdminTextSecondary,
                    fontSize = 12.sp
                )
            }

            if (report.status == "pending") {
                IconButton(onClick = onResolve) {
                    Icon(Icons.Default.CheckCircle, null, tint = AdminSuccess, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = AdminDanger, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
