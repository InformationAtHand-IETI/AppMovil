import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.eci.co.informationathand.R
import edu.eci.co.informationathand.chat.model.ChatModel
import edu.eci.co.informationathand.chat.CreateChatFragment
import edu.eci.co.informationathand.chat.SearchChatsFragment
import edu.eci.co.informationathand.databinding.FragmentCommunityChatsBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import edu.eci.co.informationathand.MainMapActivity
import edu.eci.co.informationathand.chat.viewmodel.CommunityChatsViewModel

class CommunityChatsFragment : Fragment() {

    private var _binding: FragmentCommunityChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatsAdapter
    private lateinit var viewModel: CommunityChatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityChatsBinding.inflate(inflater, container, false)
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

        viewModel = ViewModelProvider(requireActivity())[CommunityChatsViewModel::class.java]
        
        adapter = ChatsAdapter { chat ->
            val fragment = edu.eci.co.informationathand.chat.CommunityChatFragment().apply {
                arguments = Bundle().apply {
                    putString("chatId", chat.id)
                    putString("chatName", chat.name)
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            (activity as? MainMapActivity)?.hideBottomNav()
        }
        val layoutManager = LinearLayoutManager(requireContext())
        binding.chatsRecyclerView.layoutManager = layoutManager
        binding.chatsRecyclerView.adapter = adapter
        
        binding.chatsRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                ) {
                    if (viewModel.isUserChatSearchActive) {
                        viewModel.loadMoreUserChatsResults()
                    } else {
                        viewModel.loadMoreChats()
                    }
                }
            }
        })


        setupObservers()
        viewModel.refreshChats()

        binding.etSearchChats.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val query = v.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchUserChats(query)
                } else {
                    viewModel.clearUserChatSearch()
                    adapter.submitList(viewModel.chats.value ?: emptyList())
                }
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }


        binding.btnMoreOptions.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.menu_chat_options, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.action_create_chat -> {
                        val fragment = CreateChatFragment()
                        (activity as? MainMapActivity)?.hideBottomNav()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }

                    R.id.action_search_chat -> {
                        val fragment = SearchChatsFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }

    }

    private fun setupObservers() {
        viewModel.chats.observe(viewLifecycleOwner) { chats ->
            if (!viewModel.isUserChatSearchActive) {
                adapter.submitList(chats)
            }
        }
        viewModel.userChatsSearchResults.observe(viewLifecycleOwner) { chats ->
            if (viewModel.isUserChatSearchActive) {
                adapter.submitList(chats)
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show loading state if needed
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
