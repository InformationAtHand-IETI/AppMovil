package edu.eci.co.informationathand.chat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import edu.eci.co.informationathand.chat.model.ChatInfo
import edu.eci.co.informationathand.chat.model.ErrorResponse
import edu.eci.co.informationathand.chat.model.GroupChat
import edu.eci.co.informationathand.chat.model.LastMessage
import edu.eci.co.informationathand.chat.repository.ChatRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class CommunityChatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application)

    private val _chats = MutableLiveData<List<GroupChat>>()
    val chats: LiveData<List<GroupChat>> = _chats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentPage = 0
    private var isLastPage = false

    init {
        repository.init(
            onReady = {
                refreshChats()
            },
            onError = { println(it.message) }
        )
    }

    fun refreshChats() {
        if (currentPage > 0) return
        println("sssddddd")
        loadMoreChats()
    }

    fun loadMoreChats() {
        if (isLastPage || _isLoading.value == true) return

        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = repository.getUserChats(currentPage)
                val currentList = _chats.value.orEmpty().toMutableList()
                currentList.addAll(result.content)
                _chats.value = currentList
                
                isLastPage = result.pageNumber >= result.totalPages - 1
                if (!isLastPage) {
                    currentPage++
                }
            } catch (e: Exception) {
                println(e.message)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addChat(chatInfo: ChatInfo) {
        val newChat = GroupChat(
            id = chatInfo.id,
            name = chatInfo.name,
            neighborhood = chatInfo.neighborhood,
            city = "Bogota", // Default or from somewhere else
            zone = chatInfo.zone,
            memberCount = 1,
            createdAt = LocalDateTime.now().toString(),
            lastMessageInfo = LastMessage("Chat Creado", null),
            adminId = "", // Unknown
            isLastMessageMine = true
        )
        val currentList = _chats.value.orEmpty().toMutableList()
        currentList.add(0, newChat)
        _chats.value = currentList
    }
    private val _createChatResult = MutableLiveData<Result<ChatInfo>>()
    val createChatResult: LiveData<Result<ChatInfo>> = _createChatResult

    fun createChat(name: String, zone: String, neighborhood: String?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val chat = repository.createChat(name, zone, neighborhood)
                addChat(chat)
                _createChatResult.value = Result.success(chat)
            } catch (e: Exception) {
                _createChatResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Search Logic
    private val _searchNewChatsResults = MutableLiveData<List<ChatInfo>>()
    val searchNewChatsResults: LiveData<List<ChatInfo>> = _searchNewChatsResults

    private var currentSearchPage = 0
    private var isLastSearchPage = false
    private var currentQuery = ""

    fun searchNewChats(query: String) {
        currentQuery = query
        currentSearchPage = 0
        isLastSearchPage = false
        _searchNewChatsResults.value = emptyList()
        loadMoreNewChatsResults()
    }

    fun loadMoreNewChatsResults() {
        if (isLastSearchPage || _isLoading.value == true) return

        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = repository.searchChats(currentQuery, currentSearchPage)
                val currentList = _searchNewChatsResults.value.orEmpty().toMutableList()
                currentList.addAll(result.content)
                _searchNewChatsResults.value = currentList

                isLastSearchPage = result.pageNumber >= result.totalPages - 1
                if (!isLastSearchPage) {
                    currentSearchPage++
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Join Logic
    private val _joinChatResult = MutableLiveData<Event<Result<GroupChat>>>()
    val joinChatResult: LiveData<Event<Result<GroupChat>>> = _joinChatResult


    fun joinChat(chatId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val groupChat = repository.joinGroup(chatId)
                // Add to the main list
                val currentList = _chats.value.orEmpty().toMutableList()
                currentList.add(0, groupChat)
                _chats.value = currentList
                
                _joinChatResult.value = Event(Result.success(groupChat))
            } catch (e: Exception) {
                val errorMessage = parseErrorMessage(e)
                _joinChatResult.value = Event(Result.failure(Exception(errorMessage)))
                _error.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChatJoinResult(){
        _joinChatResult.value = null
    }
    private fun parseErrorMessage(e: Exception): String {
        return if (e is retrofit2.HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody == null){
                    e.message()
                }
                val error = Gson().fromJson(errorBody, ErrorResponse::class.java)
                error.message
            } catch (ex: Exception) {
                e.message()
            }
        } else {
            e.message ?: "Unknown error"
        }
    }

    // User Chats Search Logic
    private val _userChatsSearchResults = MutableLiveData<List<GroupChat>>()
    val userChatsSearchResults: LiveData<List<GroupChat>> = _userChatsSearchResults

    private var currentUserSearchPage = 0
    private var isLastUserSearchPage = false
    private var currentUserQuery = ""
    var isUserChatSearchActive = false

    fun searchUserChats(query: String) {
        currentUserQuery = query
        currentUserSearchPage = 0
        isLastUserSearchPage = false
        isUserChatSearchActive = true
        _userChatsSearchResults.value = emptyList()
        loadMoreUserChatsResults()
    }

    fun clearUserChatSearch() {
        isUserChatSearchActive = false
        currentUserQuery = ""
        _userChatsSearchResults.value = emptyList()
    }

    fun loadMoreUserChatsResults() {
        if (isLastUserSearchPage || _isLoading.value == true) return

        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = repository.searchUserChats(currentUserQuery, currentUserSearchPage)
                val currentList = _userChatsSearchResults.value.orEmpty().toMutableList()
                currentList.addAll(result.content)
                _userChatsSearchResults.value = currentList

                isLastUserSearchPage = result.pageNumber >= result.totalPages - 1
                if (!isLastUserSearchPage) {
                    currentUserSearchPage++
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    class Event<out T>(private val content: T) {
        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? =
            if (hasBeenHandled) null else {
                hasBeenHandled = true
                content
            }
    }

}
