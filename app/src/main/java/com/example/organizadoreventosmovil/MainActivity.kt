package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class Usuario(
    val username: String,
    val email: String,
    val password_text: String
)

class MainActivity : AppCompatActivity() {

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val headerBackground = findViewById<View>(R.id.headerBackground)
        val formContainer = findViewById<ScrollView>(R.id.formScrollView)

        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        headerBackground.startAnimation(slideDown)
        formContainer.startAnimation(slideUp)

        val tvEmailLabel = findViewById<TextView>(R.id.tvEmailLabel)
        val tvAuthPrompt = findViewById<TextView>(R.id.tvAuthPrompt)
        val btnToggleMode = findViewById<Button>(R.id.btnToggleMode)
        val tvFormTitle = findViewById<TextView>(R.id.tvFormTitle)
        val btnAction = findViewById<Button>(R.id.btnAction)
        val groupUsername = findViewById<LinearLayout>(R.id.groupUsername)
        val groupConfirmPassword = findViewById<LinearLayout>(R.id.groupConfirmPassword)
        
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val etUsername = findViewById<EditText>(R.id.etUsername)

        btnAction.setOnClickListener {
            val identifier = etEmail.text.toString().trim() // Puede ser email o username
            val password = etPassword.text.toString().trim()

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLoginMode) {
                loginUser(identifier, password)
            } else {
                val confirmPassword = etConfirmPassword.text.toString().trim()
                val username = etUsername.text.toString().trim()
                val email = identifier // En registro, el identifier siempre es el email

                if (password != confirmPassword) {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                if (username.isEmpty()) {
                    Toast.makeText(this, "El nombre de usuario es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!email.contains("@")) {
                    Toast.makeText(this, "Por favor introduce un email válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                registerUser(email, password, username)
            }
        }

        btnToggleMode.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                tvFormTitle.text = "Iniciar Sesión"
                btnAction.text = "Entrar"
                tvEmailLabel.text = "Email o Nombre de Usuario"
                tvAuthPrompt.text = "¿No tienes cuenta?"
                btnToggleMode.text = "Regístrate"
                groupUsername.visibility = View.GONE
                groupConfirmPassword.visibility = View.GONE
            } else {
                tvFormTitle.text = "Regístrate"
                btnAction.text = "Registrarse"
                tvEmailLabel.text = "Email"
                tvAuthPrompt.text = "¿Ya tienes cuenta?"
                btnToggleMode.text = "Inicia Sesión"
                groupUsername.visibility = View.VISIBLE
                groupConfirmPassword.visibility = View.VISIBLE
            }
        }
    }

    private fun loginUser(identifier: String, password: String) {
        lifecycleScope.launch {
            try {
                var emailToUse = identifier

                // Si no parece un email (no tiene @), buscamos el email asociado al username en la tabla 'usuarios'
                if (!identifier.contains("@")) {
                    val resultado = SupabaseClient.client.postgrest["usuarios"]
                        .select(Columns.list("email")) {
                            filter {
                                eq("username", identifier)
                            }
                        }
                        .decodeSingleOrNull<UsuarioEmailOnly>()
                    
                    if (resultado != null) {
                        emailToUse = resultado.email
                    } else {
                        Toast.makeText(this@MainActivity, "Nombre de usuario no encontrado", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }

                // Ahora que tenemos el email (ya sea el original o el recuperado), iniciamos sesión en Auth
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = emailToUse
                    this.password = password
                }
                
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registerUser(email: String, password: String, username: String) {
        lifecycleScope.launch {
            try {
                // 1. Registro en Supabase Auth
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("username", username)
                    }
                }

                // 2. Registro en tu tabla personalizada 'usuarios'
                val nuevoUsuario = Usuario(username, email, password)
                SupabaseClient.client.postgrest["usuarios"].insert(nuevoUsuario)

                Toast.makeText(this@MainActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                finish()
            } catch (e: Exception) {
                if (e.message?.contains("already registered", ignoreCase = true) == true || 
                    e.message?.contains("duplicate key", ignoreCase = true) == true) {
                    Toast.makeText(this@MainActivity, "Este usuario o correo ya existe", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error al registrarse: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Serializable
data class UsuarioEmailOnly(val email: String)
