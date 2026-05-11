package io.eshort.user.presentation.screens.inbox

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import io.eshort.user.domain.model.ChatMessage
import io.eshort.user.presentation.theme.Ash
import io.eshort.user.presentation.theme.Charcoal
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Graphite
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Mint
import io.eshort.user.presentation.theme.Mist
import io.eshort.user.presentation.theme.Slate
import io.eshort.user.presentation.theme.Snow

@Composable
fun ChatRoomScreen(
    conversationId: String,
    otherUid: String,
    onBack: () -> Unit,
    viewModel: ChatRoomViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isOtherTyping.collectAsState()
    val isOnline by viewModel.isOtherOnline.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.init(conversationId, otherUid)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Charcoal)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Snow)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Slate)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    otherUid.take(12),
                    color = Snow,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    when {
                        isTyping -> "typing..."
                        isOnline -> "Online"
                        else -> "Offline"
                    },
                    color = when {
                        isTyping -> Coral
                        isOnline -> Mint
                        else -> Ash
                    },
                    fontSize = 12.sp
                )
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(message = msg, isMine = msg.senderId == myUid)
            }

            if (isTyping) {
                item {
                    Text(
                        "typing...",
                        color = Ash,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Charcoal)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    viewModel.setTyping(it.isNotEmpty())
                },
                placeholder = { Text("Message...", color = Ash) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Snow,
                    unfocusedTextColor = Snow,
                    focusedContainerColor = Graphite,
                    unfocusedContainerColor = Graphite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Coral
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText.trim())
                        inputText = ""
                        viewModel.setTyping(false)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Coral)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    null,
                    tint = Snow,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp
                    )
                )
                .background(if (isMine) Coral else Graphite)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                message.text,
                color = Snow,
                fontSize = 14.sp
            )
        }
    }
}
