package edu.eci.co.informationathand.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.eci.co.informationathand.chat.model.ChatMessage
import edu.eci.co.informationathand.chat.network.WebSocketManager
import edu.eci.co.informationathand.chat.repository.ChatRepository
import kotlinx.coroutines.launch

class CommunityChatViewModel(application: Application) : ViewModel() {

    private val repository = ChatRepository(application)

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentChatId: String = ""

    init {
        repository.init(
            onReady = {
                loadMessages(currentChatId)
            },
            onError = { println(it.message) }
        )
        observeWebSocketMessages()
    }

    private val pendingMessageIds = mutableSetOf<String>()

    private fun observeWebSocketMessages() {
        viewModelScope.launch {
            WebSocketManager.messageFlow.collect { message ->
                handleIncomingMessage(message)
            }
        }
    }

    private fun handleIncomingMessage(message: ChatMessage) {
        val messageGroupId = message.groupId
        val currentList = messagesCache[messageGroupId] ?: mutableListOf()

        if (message.sentByMe) {
            // Try to find a pending message to replace (FIFO)
            val pendingIndex = currentList.indexOfFirst {
                pendingMessageIds.contains(it.id) && it.content == message.content
            }

            if (pendingIndex != -1) {
                // Found match, replace it with server message (with real ID)
                val pendingId = currentList[pendingIndex].id
                pendingMessageIds.remove(pendingId)
                currentList[pendingIndex] = message
                messagesCache[messageGroupId] = currentList
                
                // Only update UI if this message belongs to the current chat
                if (messageGroupId == currentChatId) {
                    _messages.value = currentList
                }
                return
            }
        }

        // No match or not sent by me, just append
        addMessage(message)
    }

    private fun addMessage(message: ChatMessage) {
        val messageGroupId = message.groupId
        val currentList = messagesCache[messageGroupId] ?: mutableListOf()
        currentList.add(message)
        messagesCache[messageGroupId] = currentList
        // Only update UI if this message belongs to the current chat
        if (messageGroupId == currentChatId) {
            _messages.value = currentList
        }
    }

    fun setChatId(chatId: String){
        currentChatId = chatId
    }

    fun sendMessage(content: String) {
        if (currentChatId.isEmpty()) return
        
        val userName = "Yo"
        // Use a temporary ID or wait for server response (optimistic update)
        val tempId = java.util.UUID.randomUUID().toString()
        val timestamp = java.time.LocalDateTime.now().toString()
        
        val message = ChatMessage(
            id = tempId,
            content = content,
            createdAt = timestamp,
            senderName = userName,
            sentByMe = true,
            userId = "", // Placeholder
            groupId = currentChatId
        )
        pendingMessageIds.add(tempId)
        addMessage(message)
        WebSocketManager.sendMessage(currentChatId, content)
    }
    // Cache
    private val messagesCache = HashMap<String, MutableList<ChatMessage>>()
    private val pageCache = HashMap<String, Int>()
    private val isLastPageCache = HashMap<String, Boolean>()

    fun loadMessages(groupId: String) {
        currentChatId = groupId
        if (messagesCache.containsKey(groupId)) {
            _messages.value = messagesCache[groupId]
        } else {
            _messages.value = emptyList()
            fetchMessages(groupId)
        }
    }

    fun fetchMessages(groupId: String) {
        val currentPage = pageCache[groupId] ?: 0
        val isLastPage = isLastPageCache[groupId] ?: false

        if (isLastPage || _isLoading.value == true) return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = repository.getChatMessages(groupId, currentPage)
                
                val currentList = messagesCache[groupId] ?: mutableListOf()
                val newMessages = result.content.reversed()

                if (currentPage == 0) {
                    currentList.clear()
                    currentList.addAll(newMessages)
                } else {
                    currentList.addAll(0, newMessages)
                }
                
                messagesCache[groupId] = currentList
                _messages.value = currentList
                
                val newIsLastPage = result.pageNumber >= result.totalPages - 1
                isLastPageCache[groupId] = newIsLastPage
                
                if (!newIsLastPage) {
                    pageCache[groupId] = currentPage + 1
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class CommunityChatViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityChatViewModel::class.java)) {
            return CommunityChatViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
