package edu.eci.co.informationathand.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.eci.co.informationathand.chat.model.ChatMessage
import edu.eci.co.informationathand.chat.repository.ChatRepository
import kotlinx.coroutines.launch

class CommunityChatViewModel(application: Application, private val chatId: String) : ViewModel() {

    private val repository = ChatRepository(application)

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        repository.init(
            onReady = {
                loadMessages(chatId)
            },
            onError = { println(it.message) }
        )
    }
    // Cache
    private val messagesCache = HashMap<String, MutableList<ChatMessage>>()
    private val pageCache = HashMap<String, Int>()
    private val isLastPageCache = HashMap<String, Boolean>()

    fun loadMessages(groupId: String) {
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
    private val application: Application,
    private val chatId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityChatViewModel::class.java)) {
            return CommunityChatViewModel(application ,chatId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
