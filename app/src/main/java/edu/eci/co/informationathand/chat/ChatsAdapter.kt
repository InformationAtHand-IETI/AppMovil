import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import edu.eci.co.informationathand.chat.model.GroupChat

import edu.eci.co.informationathand.R
import edu.eci.co.informationathand.databinding.ItemChatBinding
import java.time.format.DateTimeFormatter

class ChatsAdapter(private val onChatClick: (GroupChat) -> Unit) :
    RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    private var chats: List<GroupChat> = emptyList()

    fun submitList(list: List<GroupChat>) {
        chats = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount(): Int = chats.size

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: GroupChat) {
            binding.tvChatName.text = chat.name
            binding.tvLastMessage.text = chat.lastMessageInfo.content ?: "Chat Creado"
            binding.tvMessageTime.text = chat.lastMessageInfo.sentAt ?: chat.createdAt

            // Evento de click
            binding.root.setOnClickListener {
                onChatClick(chat)
            }
        }
    }
}
