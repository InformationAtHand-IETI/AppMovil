package edu.eci.co.informationathand.chat.model

data class PagedResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    var totalPages: Int
)