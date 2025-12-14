package com.example.organizadoreventosmovil

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class NuevoEvento1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento1)

        val nombreEventoEditText = findViewById<TextInputEditText>(R.id.nombreEventoEditText)
        val fechaEditText = findViewById<TextInputEditText>(R.id.fechaEditText)
        val organizadorEditText = findViewById<TextInputEditText>(R.id.organizadorEditText)
        val telefonoEditText = findViewById<TextInputEditText>(R.id.telefonoEditText)
        val participantesEditText = findViewById<TextInputEditText>(R.id.participantesEditText)
        val mesasEditText = findViewById<TextInputEditText>(R.id.mesasEditText)

        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnContinue.setOnClickListener {
            val nombreEvento = nombreEventoEditText.text.toString()
            val fecha = fechaEditText.text.toString()
            val organizador = organizadorEditText.text.toString()
            val telefono = telefonoEditText.text.toString()
            val participantes = participantesEditText.text.toString()
            val mesas = mesasEditText.text.toString()

            if (nombreEvento.isNotEmpty() && fecha.isNotEmpty() && organizador.isNotEmpty() && telefono.isNotEmpty() && participantes.isNotEmpty() && mesas.isNotEmpty()) {
                // Por ahora, solo mostramos un mensaje de éxito.
                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}
