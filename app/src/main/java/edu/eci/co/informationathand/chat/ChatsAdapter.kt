import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import edu.eci.co.informationathand.chat.ChatModel
import edu.eci.co.informationathand.chat.CommunityChatFragment
import edu.eci.co.informationathand.R
import edu.eci.co.informationathand.databinding.ItemChatBinding

class ChatsAdapter(private val originalList: List<ChatModel>, val fragmentActivity: FragmentActivity) :
    RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    private var filteredList = originalList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(text: String) {
        filteredList = if (text.isEmpty()) {
            originalList.toMutableList()
        } else {
            originalList.filter {
                it.name.contains(text, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatModel) {
            binding.tvChatName.text = chat.name
            binding.tvLastMessage.text = chat.lastMessage
            binding.tvMessageTime.text = chat.time

            // Evento de click
            binding.root.setOnClickListener {
                Toast.makeText(binding.root.context, "Abriendo ${chat.name}", Toast.LENGTH_SHORT).show()
                showFragment(CommunityChatFragment())
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
