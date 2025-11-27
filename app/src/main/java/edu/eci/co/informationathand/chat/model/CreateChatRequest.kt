package edu.eci.co.informationathand.chat.model

data class CreateChatRequest(
    val name: String,
    val city: String,
    val zone: String,
    val neighborhood: String?,
    val latitude: Double,
    val longitude: Double
)