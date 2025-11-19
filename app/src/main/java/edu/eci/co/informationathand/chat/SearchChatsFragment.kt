package edu.eci.co.informationathand.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.eci.co.informationathand.databinding.FragmentSearchChatsBinding

class SearchChatsFragment : Fragment() {

    private lateinit var binding: FragmentSearchChatsBinding
    private lateinit var adapter: ChatSearchListAdapter

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
            v.setPadding(
                v.paddingLeft,
                statusBar.top,   // padding = altura real del notch / barra de estado
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        adapter = ChatSearchListAdapter()
        binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChats.adapter = adapter

        val chats = listOf(
            ChatSearch("Chat Juvenil", "Norte", "Cedritos"),
            ChatSearch("Seguridad Ciudadana", "Centro", "La Candelaria"),
            ChatSearch("Mascotas Perdidas", "Sur", "San Carlos"),
            ChatSearch("Comunidad Ambiental", "Occidente", "Modelia"),
            ChatSearch("Deportes y Recreación", "Norte", "Santa Bárbara")
        )

        adapter.submitList(chats)

        binding.etSearchChats.addTextChangedListener{ text ->
            val query = text.toString()
            adapter.filter(query)
        }
    }
}
