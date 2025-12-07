package edu.eci.co.informationathand.chat.fragment

import CommunityChatsFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.eci.co.informationathand.MainMapActivity
import edu.eci.co.informationathand.R
import edu.eci.co.informationathand.chat.adapter.ChatMessageAdapter
import edu.eci.co.informationathand.chat.viewmodel.CommunityChatViewModel
import edu.eci.co.informationathand.chat.viewmodel.CommunityChatViewModelFactory
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
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                statusBar.top,   // padding = altura real del notch / barra de estado
                v.paddingRight,
                systemBars.bottom
            )
            insets
        }
        val chatId = arguments?.getString("chatId") ?: return
        val chatName = arguments?.getString("chatName") ?: ""
        
        binding.tvChatTitle.text = chatName
        
        val viewModel = ViewModelProvider(requireActivity(), CommunityChatViewModelFactory(requireActivity().application))
            .get(CommunityChatViewModel::class.java)
        viewModel.loadMessages(chatId)
        adapter = ChatMessageAdapter(emptyList())
        val layoutManager = LinearLayoutManager(requireContext())
        binding.messagesRecyclerView.layoutManager = layoutManager
        binding.messagesRecyclerView.adapter = adapter
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
             val oldSize = adapter.itemCount
             adapter.updateMessages(messages)
             val newSize = adapter.itemCount
             
             if (oldSize == 0 && newSize > 0) {
                 binding.messagesRecyclerView.scrollToPosition(newSize - 1)
             } else if (newSize > oldSize) {
                 val newItemsCount = newSize - oldSize
                 binding.messagesRecyclerView.scrollToPosition(newItemsCount)
             }
        }
        binding.messagesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(-1)) {
                    viewModel.fetchMessages(chatId)
                }
            }
        })


        binding.btnBack.setOnClickListener {
            val fragment = CommunityChatsFragment()
            (activity as? MainMapActivity)?.showBottomNav()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnSend.setOnClickListener {
            val content = binding.etMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                viewModel.sendMessage(content)
                binding.etMessage.text.clear()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}