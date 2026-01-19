package com.example.organizadoreventosmovil

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class NuevoEvento1Activity : AppCompatActivity() {

    // Variable técnica para Supabase (YYYY-MM-DD)
    private var fechaIsoParaNube: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento1)

        val nombreEventoEditText = findViewById<TextInputEditText>(R.id.nombreEventoEditText)
        val fechaEditText = findViewById<TextInputEditText>(R.id.fechaEditText)
        val organizadorEditText = findViewById<TextInputEditText>(R.id.organizadorEditText)
        val mesasEditText = findViewById<TextInputEditText>(R.id.mesasEditText)

        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // Configuración DatePicker
        fechaEditText.isFocusable = false
        fechaEditText.setOnClickListener {
            val calendario = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val fechaVisible = String.format("%02d/%02d/%04d", day, month + 1, year)
                fechaEditText.setText(fechaVisible)
                // Guardamos el formato ISO para evitar errores de base de datos
                fechaIsoParaNube = String.format("%04d-%02d-%02d", year, month + 1, day)
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnContinue.setOnClickListener {
            val nombre = nombreEventoEditText.text.toString().trim()
            val organizador = organizadorEditText.text.toString().trim()
            val mesasStr = mesasEditText.text.toString().trim()

            // VALIDACIÓN POR FASES (Confirmación antes de avanzar)
            if (nombre.isEmpty()) {
                nombreEventoEditText.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            if (fechaIsoParaNube.isEmpty()) {
                Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val numMesas = mesasStr.toIntOrNull()
            if (numMesas == null || numMesas <= 0) {
                mesasEditText.error = "Introduce un número válido de mesas"
                return@setOnClickListener
            }

            // SI TODO ESTÁ BIEN: Confirmamos visualmente y pasamos a la fase 2
            Toast.makeText(this, "✅ Datos básicos correctos", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, NuevoEvento2Activity::class.java)
            intent.putExtra("NOMBRE_EVENTO", nombre)
            intent.putExtra("FECHA_EVENTO", fechaIsoParaNube)
            intent.putExtra("LUGAR_EVENTO", organizador)
            intent.putExtra("NUMERO_MESAS", numMesas)
            startActivity(intent)
        }

        btnBack.setOnClickListener { finish() }
    }
}