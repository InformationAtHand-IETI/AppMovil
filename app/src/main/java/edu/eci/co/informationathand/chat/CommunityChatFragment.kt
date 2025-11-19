package edu.eci.co.informationathand.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.eci.co.informationathand.databinding.FragmentChatOpenBinding

class CommunityChatFragment(): Fragment(){
    private var _binding : FragmentChatOpenBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatOpenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(
                v.paddingLeft,
                statusBar.top,   // padding = altura real del notch / barra de estado
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        val sampleMessages = listOf(
            ChatMessage("Un mensaje bonito", "10:20 PM","dios" ,false ),
            ChatMessage("Un mensaje bonito", "10:20 PM","dios" ,false ),
            ChatMessage("Un mensaje bonito", "10:20 PM","dios" ,false ),
            ChatMessage("Un mensaje bonito", "10:20 PM","dios" ,false ),
            ChatMessage("Un mensaje bonito", "10:20 PM","dios" ,true ),
            ChatMessage("Un mensaje bonito", "10:20 PM","dios" ,false ),
        )
        adapter = ChatMessageAdapter(sampleMessages)
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.messagesRecyclerView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}