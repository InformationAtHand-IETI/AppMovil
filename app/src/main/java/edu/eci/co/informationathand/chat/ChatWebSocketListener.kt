package edu.eci.co.informationathand.chat

import okhttp3.WebSocket
import okhttp3.WebSocketListener

class ChatWebSocketListener(
    private val onMessageReceived: (String) -> Unit,
    private val onOpenConnection: () -> Unit,
    private val onClosed: () -> Unit
) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        onOpenConnection()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        onMessageReceived(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        t.printStackTrace()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        onClosed()
    }
}
