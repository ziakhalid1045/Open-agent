package io.eshort.user.presentation.screens.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eshort.user.domain.model.ChatMessage
import io.eshort.user.domain.repository.ChatRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val chatRepo: ChatRepo
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isOtherTyping = MutableStateFlow(false)
    val isOtherTyping: StateFlow<Boolean> = _isOtherTyping.asStateFlow()

    private val _isOtherOnline = MutableStateFlow(false)
    val isOtherOnline: StateFlow<Boolean> = _isOtherOnline.asStateFlow()

    private var conversationId: String = ""

    fun init(convoId: String, otherUid: String) {
        conversationId = convoId
        chatRepo.setOnlineStatus(true)

        viewModelScope.launch {
            chatRepo.messages(convoId).collect { _messages.value = it }
        }
        viewModelScope.launch {
            chatRepo.observeTyping(convoId, otherUid).collect { _isOtherTyping.value = it }
        }
        viewModelScope.launch {
            chatRepo.observeOnline(otherUid).collect { _isOtherOnline.value = it }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            chatRepo.sendMessage(conversationId, text)
        }
    }

    fun setTyping(isTyping: Boolean) {
        chatRepo.setTyping(conversationId, isTyping)
    }

    override fun onCleared() {
        chatRepo.setOnlineStatus(false)
        if (conversationId.isNotEmpty()) {
            chatRepo.setTyping(conversationId, false)
        }
    }
}
