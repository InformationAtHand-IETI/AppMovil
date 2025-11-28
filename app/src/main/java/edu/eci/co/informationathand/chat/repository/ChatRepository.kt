package edu.eci.co.informationathand.chat.repository

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import edu.eci.co.informationathand.MsalAuthManager
import edu.eci.co.informationathand.R
import edu.eci.co.informationathand.chat.model.ChatInfo
import edu.eci.co.informationathand.chat.model.ChatMessage
import edu.eci.co.informationathand.chat.model.CreateChatRequest
import edu.eci.co.informationathand.chat.model.GroupChat
import edu.eci.co.informationathand.chat.model.PagedResponse
import edu.eci.co.informationathand.chat.network.ChatApiService
import edu.eci.co.informationathand.chat.network.RetrofitClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ChatRepository(private val context: Context) {
    private lateinit var api: ChatApiService
    private var msalAuthManager: MsalAuthManager = MsalAuthManager(context, R.raw.auth_config_single_account)

    fun init(onReady: () -> Unit, onError: (Exception) -> Unit) {
        msalAuthManager.initialize(
            onReady = {
                api = RetrofitClient.create(msalAuthManager)
                onReady()
            },
            onError = { ex -> onError(ex) }
        )
    }

    suspend fun getUserChats(page: Int = 0, size: Int = 10): PagedResponse<GroupChat> = api.getUserChats(page, size)
    suspend fun searchUserChats(search: String, page: Int = 0, size: Int = 10): PagedResponse<GroupChat> =
        api.searchUserChats(search, page, size)
    suspend fun searchChats(query: String, page: Int = 0, size: Int = 10): PagedResponse<ChatInfo> =
        api.searchChats(query, page, size)
    suspend fun joinGroup(groupId: String): GroupChat =
        api.joinGroup(groupId)
    suspend fun getChatMessages(groupId: String, page: Int = 0, size: Int = 10 ) : PagedResponse<ChatMessage> =
        api.getChatMessages(groupId, page, size)
    suspend fun createChat(name: String, zone: String, neighborhood: String?) : ChatInfo {
        val (lat, lon) = getCoordinatesSuspend(context)

        return api.createChat(
            CreateChatRequest(
                name = name,
                city = "Bogota",
                zone = zone,
                neighborhood = neighborhood,
                latitude = lat,
                longitude = lon
            )
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun getCoordinatesSuspend(context: Context): Pair<Double, Double> =
        suspendCancellableCoroutine { cont ->

            val fused = LocationServices.getFusedLocationProviderClient(context)

            fused.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(Pair(location.latitude, location.longitude))
                } else {
                    fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { newLocation ->
                            if (newLocation != null) {
                                cont.resume(Pair(newLocation.latitude, newLocation.longitude))
                            } else {
                                cont.resume(Pair(Double.NaN, Double.NaN))
                            }
                        }.addOnFailureListener {
                            cont.resume(Pair(Double.NaN, Double.NaN))
                        }
                }
            }.addOnFailureListener {
                cont.resume(Pair(Double.NaN, Double.NaN))
            }
        }
}
