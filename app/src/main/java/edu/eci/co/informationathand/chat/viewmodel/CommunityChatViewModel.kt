package edu.eci.co.informationathand.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import edu.eci.co.informationathand.chat.model.ChatMessage
import edu.eci.co.informationathand.chat.repository.ChatRepository
import kotlinx.coroutines.launch

class CommunityChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application)

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

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
