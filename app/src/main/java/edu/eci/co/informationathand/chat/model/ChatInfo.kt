package edu.eci.co.informationathand.chat.model

data class ChatInfo(
    val id: String,
    val name: String,
    val zone: String,
    val neighborhood: String?,
    val latitude: Double,
    val longitude: Double
) : java.io.Serializable