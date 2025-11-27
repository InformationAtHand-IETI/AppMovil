package edu.eci.co.informationathand.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import edu.eci.co.informationathand.chat.model.ChatInfo
import edu.eci.co.informationathand.databinding.ItemChatSearchBinding

class ChatSearchListAdapter(
    private val onJoinClick: (ChatInfo) -> Unit
) : RecyclerView.Adapter<ChatSearchListAdapter.ChatViewHolder>() {

    private var originalList: List<ChatInfo> = emptyList()
    private var filteredList: List<ChatInfo> = emptyList()

    fun submitList(list: List<ChatInfo>) {
        originalList = list
        filteredList = list
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val search = query.lowercase().trim()

        filteredList = if (search.isEmpty()) {
            originalList
        } else {
            originalList.filter { chat ->
                chat.name.lowercase().contains(search) ||
                        chat.zone.lowercase().contains(search) ||
                        (chat.neighborhood?.lowercase()?.contains(search) == true)
            }
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun getItemCount() = filteredList.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    inner class ChatViewHolder(private val binding: ItemChatSearchBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatInfo) {
            binding.tvChatName.text = chat.name
            binding.tvChatInfo.text = "Zona ${chat.zone} | Barrio ${chat.neighborhood ?: "N/A"}"
            binding.btnJoin.setOnClickListener {
                onJoinClick(chat)
            }
        }
    }
}
