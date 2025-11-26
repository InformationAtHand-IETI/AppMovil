package edu.eci.co.informationathand.chat

data class ChatMessage (
    val message: String,
    val sent: String,
    val sender: String,
    val isSent: Boolean
)