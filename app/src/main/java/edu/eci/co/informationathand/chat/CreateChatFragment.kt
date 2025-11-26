package edu.eci.co.informationathand.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import edu.eci.co.informationathand.databinding.FragmentCreateChatBinding

class CreateChatFragment : Fragment() {

    private lateinit var binding: FragmentCreateChatBinding

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

        binding.btnCreateChat.setOnClickListener {
            val name = binding.etChatName.text.toString().trim()
            val zone = binding.etZone.text.toString().trim()
            val neighborhood = binding.etNeighborhood.text.toString().trim()

            if (name.isEmpty() || zone.isEmpty()) {
                Toast.makeText(requireContext(), "Los campos de nombre y zona son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Aquí enviarías los datos a tu backend
            Toast.makeText(requireContext(), "Chat creado: $name", Toast.LENGTH_SHORT).show()

            // Regresar
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}
