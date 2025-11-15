package edu.eci.co.informationathand

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import edu.eci.co.informationathand.utils.StorageHelper

class ProfileFragment : Fragment() {

    private lateinit var storageHelper: StorageHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageHelper = StorageHelper(requireContext())

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Mostrar datos del usuario
        tvUserName.text = storageHelper.getUserName() ?: "Usuario"
        tvUserEmail.text = storageHelper.getUserEmail() ?: "email@ejemplo.com"

        // Cerrar sesión
        btnLogout.setOnClickListener {
            storageHelper.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}