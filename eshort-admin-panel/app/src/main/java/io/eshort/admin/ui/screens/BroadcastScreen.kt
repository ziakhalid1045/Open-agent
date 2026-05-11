package io.eshort.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.eshort.admin.ui.theme.AdminAsh
import io.eshort.admin.ui.theme.AdminCoral
import io.eshort.admin.ui.theme.AdminGraphite
import io.eshort.admin.ui.theme.AdminJet
import io.eshort.admin.ui.theme.AdminMint
import io.eshort.admin.ui.theme.AdminMist
import io.eshort.admin.ui.theme.AdminSnow
import io.eshort.admin.viewmodel.AdminViewModel

@Composable
fun BroadcastScreen(viewModel: AdminViewModel) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val result by viewModel.broadcastResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminJet)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Broadcast", color = AdminSnow, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("Send notification to all users", color = AdminAsh, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(28.dp))

        Text("Title", color = AdminMist, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Notification title", color = AdminAsh) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = AdminSnow,
                unfocusedTextColor = AdminSnow,
                focusedContainerColor = AdminGraphite,
                unfocusedContainerColor = AdminGraphite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AdminCoral
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Message", color = AdminMist, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = body,
            onValueChange = { body = it },
            placeholder = { Text("Write your message...", color = AdminAsh) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = AdminSnow,
                unfocusedTextColor = AdminSnow,
                focusedContainerColor = AdminGraphite,
                unfocusedContainerColor = AdminGraphite,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AdminCoral
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            maxLines = 6
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.sendBroadcast(title, body)
                title = ""
                body = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AdminCoral),
            enabled = title.isNotBlank() && body.isNotBlank()
        ) {
            Text("Send Broadcast", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (result != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                result.orEmpty(),
                color = if (result?.startsWith("Failed") == true) AdminCoral else AdminMint,
                fontSize = 14.sp
            )
        }
    }
}
