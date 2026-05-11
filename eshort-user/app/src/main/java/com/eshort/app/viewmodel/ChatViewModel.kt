package com.eshort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eshort.app.data.model.ChatMessage
import com.eshort.app.data.model.ChatRoom
import com.eshort.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListState(
    val chatRooms: List<ChatRoom> = emptyList(),
    val isLoading: Boolean = true
)

data class ChatDetailState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val otherUserTyping: Boolean = false,
    val otherUserOnline: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatListState = MutableStateFlow(ChatListState())
    val chatListState: StateFlow<ChatListState> = _chatListState.asStateFlow()

    private val _chatDetailState = MutableStateFlow(ChatDetailState())
    val chatDetailState: StateFlow<ChatDetailState> = _chatDetailState.asStateFlow()

    init {
        loadChatRooms()
        chatRepository.setOnlineStatus(true)
    }

    fun loadChatRooms() {
        viewModelScope.launch {
            chatRepository.getChatRooms().collect { rooms ->
                _chatListState.value = ChatListState(chatRooms = rooms, isLoading = false)
            }
        }
    }

    fun loadMessages(chatRoomId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(chatRoomId).collect { messages ->
                _chatDetailState.value = _chatDetailState.value.copy(
                    messages = messages,
                    isLoading = false
                )
            }
        }
    }

    fun sendMessage(chatRoomId: String, text: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(chatRoomId, text)
            chatRepository.setTypingStatus(chatRoomId, false)
        }
    }

    fun startChat(otherUserId: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = chatRepository.getOrCreateChatRoom(otherUserId)
            result.onSuccess { onResult(it) }
        }
    }

    fun setTyping(chatRoomId: String, isTyping: Boolean) {
        chatRepository.setTypingStatus(chatRoomId, isTyping)
    }

    fun observeTypingAndOnline(chatRoomId: String, otherUserId: String) {
        viewModelScope.launch {
            chatRepository.observeTyping(chatRoomId).collect { typingMap ->
                _chatDetailState.value = _chatDetailState.value.copy(
                    otherUserTyping = typingMap[otherUserId] == true
                )
            }
        }
        viewModelScope.launch {
            chatRepository.observeOnlineStatus(otherUserId).collect { isOnline ->
                _chatDetailState.value = _chatDetailState.value.copy(
                    otherUserOnline = isOnline
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.setOnlineStatus(false)
    }
}
