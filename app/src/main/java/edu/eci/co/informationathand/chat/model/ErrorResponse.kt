package edu.eci.co.informationathand.chat.model

data class ErrorResponse(
    val message: String,
    val code: Int,
    val createdAt: String
)
