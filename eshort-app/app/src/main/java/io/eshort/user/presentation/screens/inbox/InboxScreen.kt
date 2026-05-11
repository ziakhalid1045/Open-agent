package io.eshort.user.presentation.screens.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import io.eshort.user.domain.model.Conversation
import io.eshort.user.presentation.theme.Ash
import io.eshort.user.presentation.theme.Coral
import io.eshort.user.presentation.theme.Graphite
import io.eshort.user.presentation.theme.Jet
import io.eshort.user.presentation.theme.Mist
import io.eshort.user.presentation.theme.Slate
import io.eshort.user.presentation.theme.Snow

@Composable
fun InboxScreen(
    onConversationClick: (conversationId: String, otherUid: String) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Jet)
            .statusBarsPadding()
    ) {
        Text(
            "Messages",
            color = Snow,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MailOutline,
                        null,
                        tint = Ash,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No messages yet", color = Snow, fontSize = 18.sp)
                    Text(
                        "Start a conversation from someone's profile",
                        color = Ash,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(conversations) { convo ->
                    val otherUid = convo.members.firstOrNull { it != myUid }.orEmpty()
                    val unread = convo.unreadCount[myUid] ?: 0
                    ConversationItem(
                        conversation = convo,
                        otherUid = otherUid,
                        unreadCount = unread,
                        onClick = { onConversationClick(convo.id, otherUid) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    otherUid: String,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Slate)
            )
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Coral)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$unreadCount",
                        color = Snow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                otherUid.take(12),
                color = Snow,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                conversation.lastText,
                color = Mist,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
