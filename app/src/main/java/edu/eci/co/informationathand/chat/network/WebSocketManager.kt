package edu.eci.co.informationathand.chat.network

import okhttp3.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import edu.eci.co.informationathand.chat.model.ChatMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WebSocketManager {
    private const val WS_URL = "wss://chatservice-etaffddncjh0bcgq.canadacentral-01.azurewebsites.net/ws?token="
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _messageFlow = MutableSharedFlow<ChatMessage>()
    val messageFlow: SharedFlow<ChatMessage> = _messageFlow.asSharedFlow()

    fun connect(token: String) {
        if (webSocket != null) return // Already connected

        val request = Request.Builder()
            .url(WS_URL + token)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                println("WebSocket Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                try {
                    val message = gson.fromJson(text, ChatMessage::class.java)
                    println("Mensaje recibido Mensaje recibido Mensaje recibido Mensaje recibido Mensaje recibido" + message)
                    scope.launch {
                        _messageFlow.emit(message)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                println("WebSocket Closing: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                println("WebSocket Closed: $reason")
                this@WebSocketManager.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                println("WebSocket Failure: ${t.message}")
                this@WebSocketManager.webSocket = null
            }
        })
    }

    fun sendMessage(chatId: String, content: String) {
        val messagePayload = mapOf("content" to content, "groupId" to chatId)
        val payload = mapOf("type" to "SEND_MESSAGE", "message" to messagePayload)
        val json = gson.toJson(payload)
        webSocket?.send(json)
    }
    
    fun close() {
        webSocket?.close(1000, "User Logout")
        webSocket = null
    }
}
