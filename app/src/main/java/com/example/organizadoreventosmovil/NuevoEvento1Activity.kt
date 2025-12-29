package com.example.organizadoreventosmovil

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class NuevoEvento1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento1)

        val headerTitle = findViewById<TextView>(R.id.headerTitle)
        val headerSubtitle = findViewById<TextView>(R.id.headerSubtitle)

        val nombreEventoEditText = findViewById<TextInputEditText>(R.id.nombreEventoEditText)
        val fechaEditText = findViewById<TextInputEditText>(R.id.fechaEditText)
        val organizadorEditText = findViewById<TextInputEditText>(R.id.organizadorEditText)
        val telefonoEditText = findViewById<TextInputEditText>(R.id.telefonoEditText)
        val participantesEditText = findViewById<TextInputEditText>(R.id.participantesEditText)
        val mesasEditText = findViewById<TextInputEditText>(R.id.mesasEditText)

        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // Verificar si estamos en modo edición
        val isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)

        if (isEditMode) {
            headerTitle.text = "Editar Evento"
            headerSubtitle.text = "Modifica los detalles del evento"
            btnContinue.text = "Guardar Cambios"

            // Rellenar campos con datos recibidos
            nombreEventoEditText.setText(intent.getStringExtra("NOMBRE_EVENTO"))
            fechaEditText.setText(intent.getStringExtra("FECHA_EVENTO"))
            organizadorEditText.setText(intent.getStringExtra("LUGAR_EVENTO"))
            
            // Datos simulados para los campos que no venían en el objeto Evento simple
            telefonoEditText.setText("600123456")
            participantesEditText.setText("150")
            mesasEditText.setText("15")
        }

        btnContinue.setOnClickListener {
            val nombreEvento = nombreEventoEditText.text.toString()
            val fecha = fechaEditText.text.toString()
            val organizador = organizadorEditText.text.toString()
            val telefono = telefonoEditText.text.toString()
            val participantes = participantesEditText.text.toString()
            val mesasStr = mesasEditText.text.toString()

            if (nombreEvento.isNotEmpty() && fecha.isNotEmpty() && organizador.isNotEmpty() && telefono.isNotEmpty() && participantes.isNotEmpty() && mesasStr.isNotEmpty()) {
                
                if (isEditMode) {
                    // En modo edición, simplemente guardamos y salimos
                    Toast.makeText(this, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // En modo creación, vamos al paso 2
                    val intent = Intent(this, NuevoEvento2Activity::class.java)
                    // Pasamos el número de mesas a la siguiente actividad
                    intent.putExtra("NUMERO_MESAS", mesasStr.toIntOrNull() ?: 5)
                    startActivity(intent)
                }
                
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
