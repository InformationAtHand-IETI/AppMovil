package edu.eci.co.informationathand

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.eci.co.informationathand.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFinalizar.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }
}
