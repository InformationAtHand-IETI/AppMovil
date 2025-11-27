package edu.eci.co.informationathand.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import edu.eci.co.informationathand.chat.model.ChatMessage
import edu.eci.co.informationathand.databinding.ItemMessageReceivedBinding
import edu.eci.co.informationathand.databinding.ItemMessageSentBinding

class ChatMessageAdapter(private val messagesList: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var messages = messagesList.toMutableList()
    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sentByMe) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_RECEIVED) {
            val view = ItemMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReceivedMessageViewHolder(view)
        }else{
            val view = ItemMessageSentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SentMessageViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
    inner class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessageText.text = message.content
            binding.tvMessageTime.text = message.createdAt

            binding.root.setOnClickListener {
                Toast.makeText(binding.root.context, "Abriendo ${message.content}", Toast.LENGTH_SHORT).show()

            }
        }
    }

    inner class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessageText.text = message.content
            binding.tvMessageTime.text = message.createdAt

            binding.root.setOnClickListener {
                Toast.makeText(binding.root.context, "Abriendo ${message.content}", Toast.LENGTH_SHORT).show()

            }
        }
    }

    fun updateMessages(newMessages: List<ChatMessage>) {
        this.messages.clear()
        this.messages.addAll(newMessages)
        notifyDataSetChanged()
    }
}