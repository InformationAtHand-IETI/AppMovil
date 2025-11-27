package edu.eci.co.informationathand.chat.model

import java.time.LocalDateTime

data class GroupChat (
    val id: String,
    val name: String,
    val neighborhood: String?,
    val city: String,
    val zone: String,
    val memberCount: Int,
    val createdAt: String,
    val lastMessageInfo: LastMessage,
    val adminId: String,
    val isLastMessageMine: Boolean
)

data class LastMessage(
    val content: String?,
    val sentAt: String?
)