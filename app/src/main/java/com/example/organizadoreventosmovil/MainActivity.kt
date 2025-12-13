package com.example.organizadoreventosmovil

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvToggleMode = findViewById<TextView>(R.id.tvToggleMode)
        val tvFormTitle = findViewById<TextView>(R.id.tvFormTitle)
        val btnAction = findViewById<Button>(R.id.btnAction)
        val groupUsername = findViewById<LinearLayout>(R.id.groupUsername)
        val groupConfirmPassword = findViewById<LinearLayout>(R.id.groupConfirmPassword)

        tvToggleMode.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                // Cambiar a modo de inicio de sesión
                tvFormTitle.text = "Iniciar Sesión"
                btnAction.text = "Entrar"
                tvToggleMode.text = "¿No tienes cuenta? Regístrate"
                groupUsername.visibility = View.GONE
                groupConfirmPassword.visibility = View.GONE
            } else {
                // Cambiar a modo de registro
                tvFormTitle.text = "Regístrate"
                btnAction.text = "Registrarse"
                tvToggleMode.text = "¿Ya tienes cuenta? Inicia Sesión"
                groupUsername.visibility = View.VISIBLE
                groupConfirmPassword.visibility = View.VISIBLE
            }
        }
    }
}