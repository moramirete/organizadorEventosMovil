package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class NuevoEvento1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento1)

        val nombreEventoEditText = findViewById<TextInputEditText>(R.id.nombreEventoEditText)
        val fechaEditText = findViewById<TextInputEditText>(R.id.fechaEditText)
        val organizadorEditText = findViewById<TextInputEditText>(R.id.organizadorEditText)
        val mesasEditText = findViewById<TextInputEditText>(R.id.mesasEditText)
        
        // Asumo que hay más campos según el layout, los añado para que no falle
        val telefonoEditText = findViewById<TextInputEditText>(R.id.telefonoEditText)
        val participantesEditText = findViewById<TextInputEditText>(R.id.participantesEditText)

        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnContinue.setOnClickListener {
            // Recogemos todos los valores de los campos de texto
            val nombreEvento = nombreEventoEditText.text.toString()
            val fecha = fechaEditText.text.toString()
            val organizador = organizadorEditText.text.toString()
            val mesasStr = mesasEditText.text.toString()
            val telefono = telefonoEditText.text.toString()
            val participantes = participantesEditText.text.toString()

            // Validamos que ningún campo importante esté vacío
            if (nombreEvento.trim().isNotEmpty() && fecha.trim().isNotEmpty() && organizador.trim().isNotEmpty() && mesasStr.trim().isNotEmpty()) {
                
                // Creamos el Intent para ir a la siguiente pantalla
                val intent = Intent(this, NuevoEvento2Activity::class.java)

                // Pasamos todos los datos del evento como extras
                intent.putExtra("NOMBRE_EVENTO", nombreEvento)
                intent.putExtra("FECHA_EVENTO", fecha)
                intent.putExtra("LUGAR_EVENTO", organizador)
                intent.putExtra("NUMERO_MESAS", mesasStr.toIntOrNull() ?: 5) // Usamos un valor por defecto si la conversión falla
                
                // Iniciamos la siguiente actividad
                startActivity(intent)
                
            } else {
                // Si algún campo está vacío, mostramos un aviso
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_LONG).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
