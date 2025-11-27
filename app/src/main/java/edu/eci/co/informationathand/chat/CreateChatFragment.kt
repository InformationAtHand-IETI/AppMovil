package edu.eci.co.informationathand.chat

import CommunityChatsFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.eci.co.informationathand.MainMapActivity
import edu.eci.co.informationathand.R
import edu.eci.co.informationathand.chat.viewmodel.CommunityChatsViewModel

import edu.eci.co.informationathand.databinding.FragmentCreateChatBinding

class CreateChatFragment : Fragment() {

    private lateinit var binding: FragmentCreateChatBinding
    private lateinit var viewModel: CommunityChatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[CommunityChatsViewModel::class.java]

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

        setupObservers()

        binding.btnCreateChat.setOnClickListener {
            val name = binding.etChatName.text.toString().trim()
            val zone = binding.etZone.text.toString().trim()
            val neighborhood = binding.etNeighborhood.text.toString().trim()

            if (name.isEmpty() || zone.isEmpty()) {
                Toast.makeText(requireContext(), "Los campos de nombre y zona son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createChat(name, zone, neighborhood)
        }

        binding.btnBack.setOnClickListener {
            val fragment = CommunityChatsFragment()
            (activity as? MainMapActivity)?.showBottomNav()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnCreateChat.isEnabled = !isLoading
            // Optionally show a progress bar
        }

        viewModel.createChatResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { chat ->

                Toast.makeText(requireContext(), "Chat creado exitosamente", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
                (activity as? MainMapActivity)?.showBottomNav()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error al crear chat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
