package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class HomeActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvUsername = findViewById(R.id.tvUsername)
        val headerBackground = findViewById<ConstraintLayout>(R.id.headerBackground)
        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)

        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        headerBackground.startAnimation(slideDown)
        buttonContainer.startAnimation(slideUp)

        // Cargar el nombre de usuario al iniciar
        displayUsername()

        val ivMenu = findViewById<ImageView>(R.id.ivMenu)
        val btnModificarEventos = findViewById<CardView>(R.id.btnModificarEventos)
        val btnNuevoEvento = findViewById<CardView>(R.id.btnNuevoEvento)
        val btnConsultarEventos = findViewById<CardView>(R.id.btnConsultarEventos)

        ivMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.home_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_change_username -> {
                        showChangeUsernameDialog()
                        true
                    }
                    R.id.menu_change_password -> {
                        showChangePasswordDialog()
                        true
                    }
                    R.id.menu_change_email -> {
                        showChangeEmailDialog()
                        true
                    }
                    R.id.menu_logout -> {
                        lifecycleScope.launch {
                            SupabaseClient.client.auth.signOut()
                            val intent = Intent(this@HomeActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        btnModificarEventos.setOnClickListener {
            val intent = Intent(this, ModificarEvento1Activity::class.java)
            startActivity(intent)
        }

        btnNuevoEvento.setOnClickListener {
            val intent = Intent(this, NuevoEvento1Activity::class.java)
            startActivity(intent)
        }

        btnConsultarEventos.setOnClickListener {
            val intent = Intent(this, VisualizarEvento1Activity::class.java)
            startActivity(intent)
        }
    }

    private fun displayUsername() {
        val user = SupabaseClient.client.auth.currentUserOrNull()
        val username = user?.userMetadata?.get("username")?.jsonPrimitive?.contentOrNull ?: "Usuario"
        tvUsername.text = "Bienvenido, $username"
    }

    private fun showChangeUsernameDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cambiar nombre de usuario")

        val input = EditText(this)
        input.hint = "Nuevo nombre de usuario"
        builder.setView(input)

        builder.setPositiveButton("Actualizar") { _, _ ->
            val newUsername = input.text.toString().trim()
            if (newUsername.isNotEmpty()) {
                updateUsername(newUsername)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun updateUsername(newUsername: String) {
        lifecycleScope.launch {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val currentEmail = user?.email ?: return@launch

                // 1. Actualizar en la tabla 'usuarios'
                SupabaseClient.client.postgrest["usuarios"].update({
                    set("username", newUsername)
                }) {
                    filter {
                        eq("email", currentEmail)
                    }
                }

                // 2. Actualizar metadatos en Auth
                SupabaseClient.client.auth.updateUser {
                    data = buildJsonObject {
                        put("username", newUsername)
                    }
                }

                // 3. Refrescar UI
                tvUsername.text = "Bienvenido, $newUsername"

                Toast.makeText(this@HomeActivity, "Nombre de usuario actualizado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cambiar contraseña")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 10)

        val oldPasswordInput = EditText(this)
        oldPasswordInput.hint = "Contraseña actual"
        oldPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(oldPasswordInput)

        val newPasswordInput = EditText(this)
        newPasswordInput.hint = "Nueva contraseña"
        newPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(newPasswordInput)

        builder.setView(layout)

        builder.setPositiveButton("Actualizar") { _, _ ->
            val oldPass = oldPasswordInput.text.toString()
            val newPass = newPasswordInput.text.toString()
            if (oldPass.isNotEmpty() && newPass.isNotEmpty()) {
                updatePassword(oldPass, newPass)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun updatePassword(oldPass: String, newPass: String) {
        lifecycleScope.launch {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val email = user?.email ?: return@launch

                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = oldPass
                }

                SupabaseClient.client.auth.updateUser {
                    password = newPass
                }

                SupabaseClient.client.postgrest["usuarios"].update({
                    set("password_text", newPass)
                }) {
                    filter {
                        eq("email", email)
                    }
                }

                Toast.makeText(this@HomeActivity, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Error: Contraseña actual incorrecta", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showChangeEmailDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cambiar correo electrónico")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 10)

        val passwordInput = EditText(this)
        passwordInput.hint = "Contraseña actual"
        passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(passwordInput)

        val newEmailInput = EditText(this)
        newEmailInput.hint = "Nuevo correo electrónico"
        newEmailInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        layout.addView(newEmailInput)

        builder.setView(layout)

        builder.setPositiveButton("Actualizar") { _, _ ->
            val pass = passwordInput.text.toString()
            val newEmail = newEmailInput.text.toString().trim()
            if (pass.isNotEmpty() && newEmail.isNotEmpty()) {
                updateEmail(pass, newEmail)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun updateEmail(pass: String, newEmail: String) {
        lifecycleScope.launch {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val oldEmail = user?.email ?: return@launch

                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = oldEmail
                    this.password = pass
                }

                SupabaseClient.client.postgrest["usuarios"].update({
                    set("email", newEmail)
                }) {
                    filter {
                        eq("email", oldEmail)
                    }
                }

                SupabaseClient.client.auth.updateUser {
                    email = newEmail
                }

                Toast.makeText(this@HomeActivity, "Correo actualizado.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Error: Contraseña incorrecta o email ya en uso", Toast.LENGTH_LONG).show()
            }
        }
    }
}
