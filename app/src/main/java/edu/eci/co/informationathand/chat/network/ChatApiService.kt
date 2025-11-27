package edu.eci.co.informationathand.chat.network

import edu.eci.co.informationathand.chat.model.ChatInfo
import edu.eci.co.informationathand.chat.model.ChatMessage
import edu.eci.co.informationathand.chat.model.CreateChatRequest
import edu.eci.co.informationathand.chat.model.GroupChat
import edu.eci.co.informationathand.chat.model.PagedResponse
import retrofit2.http.*

interface ChatApiService {

    @GET("v1/groups/search")
    suspend fun searchChats(
        @Query("search") search: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PagedResponse<ChatInfo>

    @GET("v1/groups/{groupId}/chat/messages")
    suspend fun getChatMessages(
        @Path("groupId") groupId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PagedResponse<ChatMessage>

    @GET("v1/groups")
    suspend fun getUserChats(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PagedResponse<GroupChat>

    @GET("v1/groups/users/search")
    suspend fun searchUserChats(
        @Query("search") search: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PagedResponse<GroupChat>

    @POST("v1/groups")
    suspend fun createChat(@Body chat: CreateChatRequest): ChatInfo
    @POST("v1/groups/{groupId}/join")
    suspend fun joinGroup(
        @Path("groupId") groupId: String
    ): GroupChat
}
