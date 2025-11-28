package edu.eci.co.informationathand.chat

import CommunityChatsFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import edu.eci.co.informationathand.R
import edu.eci.co.informationathand.chat.viewmodel.CommunityChatsViewModel
import edu.eci.co.informationathand.databinding.FragmentSearchChatsBinding

class SearchChatsFragment : Fragment() {

    private lateinit var binding: FragmentSearchChatsBinding
    private lateinit var adapter: ChatSearchListAdapter
    private lateinit var viewModel: CommunityChatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                statusBar.top,
                v.paddingRight,
                systemBars.bottom
            )
            insets
        }
        viewModel = ViewModelProvider(requireActivity())[CommunityChatsViewModel::class.java]
        adapter = ChatSearchListAdapter { chat ->
            viewModel.joinChat(chat.id)
        }
        binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChats.adapter = adapter

        setupObservers()

        binding.etSearchChats.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val query = v.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchNewChats(query)
                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
                true
            } else {
                false
            }
        }

        binding.btnBack.setOnClickListener {
            val fragment = CommunityChatsFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupObservers() {
        viewModel.searchNewChatsResults.observe(viewLifecycleOwner) { chats ->
            adapter.submitList(chats)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                // android.widget.Toast.makeText(requireContext(), error, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show loading state if needed
        }

        viewModel.joinChatResult.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    Toast.makeText(requireContext(), "Unido al grupo exitosamente", Toast.LENGTH_SHORT).show()

                    val fragment = CommunityChatsFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                result.onFailure {
                    Toast.makeText(
                        requireContext(),
                        "Error al unirse al grupo: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }
}
