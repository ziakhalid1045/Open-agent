package io.eshort.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.eshort.admin.ui.theme.AdminAmber
import io.eshort.admin.ui.theme.AdminAsh
import io.eshort.admin.ui.theme.AdminCharcoal
import io.eshort.admin.ui.theme.AdminJet
import io.eshort.admin.ui.theme.AdminMint
import io.eshort.admin.ui.theme.AdminMist
import io.eshort.admin.ui.theme.AdminRose
import io.eshort.admin.ui.theme.AdminSnow
import io.eshort.admin.viewmodel.AdminViewModel

@Composable
fun ReportsScreen(viewModel: AdminViewModel) {
    val reports by viewModel.reports.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminJet)
            .statusBarsPadding()
    ) {
        Text(
            "Reports",
            color = AdminSnow,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        Text(
            "${reports.size} reports",
            color = AdminAsh,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reports) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCharcoal)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                report.reason,
                                color = AdminSnow,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Row {
                                Text(
                                    "by @${report.reporterName}",
                                    color = AdminMist,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Target: ${report.targetType}",
                                    color = AdminAsh,
                                    fontSize = 12.sp
                                )
                            }
                            val statusColor = when (report.status) {
                                "pending" -> AdminAmber
                                "resolved" -> AdminMint
                                else -> AdminAsh
                            }
                            Text(
                                report.status.uppercase(),
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { viewModel.resolveReport(report.id) }) {
                            Icon(Icons.Default.CheckCircle, null, tint = AdminMint, modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = { viewModel.dismissReport(report.id) }) {
                            Icon(Icons.Default.Close, null, tint = AdminRose, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}
