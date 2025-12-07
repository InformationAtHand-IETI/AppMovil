package edu.eci.co.informationathand.chat.model

data class ChatMessage (
    val id: String,
    val content: String,
    val createdAt: String,
    val senderName: String,
    val sentByMe: Boolean,
    val userId: String,
    val groupId: String
)