import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.eci.co.informationathand.R
import edu.eci.co.informationathand.chat.ChatModel
import edu.eci.co.informationathand.chat.CreateChatFragment
import edu.eci.co.informationathand.chat.SearchChatsFragment
import edu.eci.co.informationathand.databinding.FragmentCommunityChatsBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener

class CommunityChatsFragment : Fragment() {

    private var _binding: FragmentCommunityChatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatsAdapter

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
            v.setPadding(
                v.paddingLeft,
                statusBar.top,   // padding = altura real del notch / barra de estado
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        // Lista de ejemplo
        val sampleChats = listOf(
            ChatModel("Seguridad Sector Norte", "Vecinos, recuerden la reunión de hoy", "12:00 PM"),
            ChatModel("Comunidad Ambiental", "Nueva jornada de reciclaje este sábado", "12:01 PM"),
            ChatModel("Comité de Movilidad", "Reporte de trancón en la 9na", "17:00 PM"),
        )

        adapter = ChatsAdapter(sampleChats, requireActivity())
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatsRecyclerView.adapter = adapter

        binding.etSearchChats.addTextChangedListener { text ->
            val query = text.toString()
            adapter.filter(query)
        }


        binding.btnMoreOptions.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.menu_chat_options, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.action_create_chat -> {
                        val fragment = CreateChatFragment()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
