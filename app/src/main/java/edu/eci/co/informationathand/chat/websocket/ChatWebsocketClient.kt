package edu.eci.co.informationathand.chat.websocket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object ChatWebSocketClient {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect(url: String, listener: WebSocketListener) {
        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Cerrado por el usuario")
    }
}
