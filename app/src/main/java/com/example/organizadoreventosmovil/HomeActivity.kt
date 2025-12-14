package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val ivMenu = findViewById<ImageView>(R.id.ivMenu)
        val btnConsultarEventos = findViewById<CardView>(R.id.btnConsultarEventos)

        ivMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.home_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_change_username -> {
                        Toast.makeText(this, "Cambiar nombre de usuario", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_change_password -> {
                        Toast.makeText(this, "Cambiar contraseña", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_change_email -> {
                        Toast.makeText(this, "Cambiar correo electrónico", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_logout -> {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        btnConsultarEventos.setOnClickListener {
            val intent = Intent(this, VisualizarEvento1Activity::class.java)
            startActivity(intent)
        }
    }
}