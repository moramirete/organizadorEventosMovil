package com.example.organizadoreventosmovil

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.organizadoreventosmovil.Constructores.Evento
import com.google.android.material.textfield.TextInputEditText
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NuevoEvento1Activity : AppCompatActivity() {

    private var fechaIsoParaNube: String = ""
    private var evento: Evento? = null
    private var isEditMode = false

    private lateinit var nombreEventoEditText: TextInputEditText
    private lateinit var fechaEditText: TextInputEditText
    private lateinit var ubicacionEditText: TextInputEditText
    private lateinit var telefonoEditText: TextInputEditText
    private lateinit var participantesEditText: TextInputEditText
    private lateinit var mesasEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento1)

        nombreEventoEditText = findViewById(R.id.nombreEventoEditText)
        fechaEditText = findViewById(R.id.fechaEditText)
        ubicacionEditText = findViewById(R.id.ubicacionEditText)
        telefonoEditText = findViewById(R.id.telefonoEditText)
        participantesEditText = findViewById(R.id.participantesEditText)
        mesasEditText = findViewById(R.id.mesasEditText)

        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnBack = findViewById<Button>(R.id.btnBack)

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        if (isEditMode) {
            val eventoJson = intent.getStringExtra("EVENTO_JSON")
            if (eventoJson != null) {
                try {
                    evento = Json.decodeFromString<Evento>(eventoJson)
                    cargarDatosDelEvento(evento!!)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fechaEditText.isFocusable = false
        fechaEditText.setOnClickListener {
            val calendario = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val fechaVisible = String.format("%02d/%02d/%04d", day, month + 1, year)
                fechaEditText.setText(fechaVisible)
                fechaIsoParaNube = String.format("%04d-%02d-%02d", year, month + 1, day)
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnContinue.setOnClickListener {
            val nombre = nombreEventoEditText.text.toString().trim()
            val organizador = ubicacionEditText.text.toString().trim()
            val telefono = telefonoEditText.text.toString().trim()
            val participantesStr = participantesEditText.text.toString().trim()
            val mesasStr = mesasEditText.text.toString().trim()

            if (nombre.isEmpty()) {
                nombreEventoEditText.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            if (fechaIsoParaNube.isEmpty() && !isEditMode) {
                Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val numMesas = mesasStr.toIntOrNull()
            if (numMesas == null || numMesas <= 0) {
                mesasEditText.error = "Introduce un número válido de mesas"
                return@setOnClickListener
            }

            val numParticipantes = participantesStr.toIntOrNull()
            if (numParticipantes == null || numParticipantes <= 0) {
                participantesEditText.error = "Introduce un número válido de participantes"
                return@setOnClickListener
            }

            val intent = Intent(this, NuevoEvento2Activity::class.java)
            intent.putExtra("NOMBRE_EVENTO", nombre)
            intent.putExtra("FECHA_EVENTO", if (fechaIsoParaNube.isNotEmpty()) fechaIsoParaNube else evento?.fecha)
            intent.putExtra("LUGAR_EVENTO", organizador)
            intent.putExtra("TELEFONO_EVENTO", telefono)
            intent.putExtra("NUM_PARTICIPANTES", numParticipantes)
            intent.putExtra("NUMERO_MESAS", numMesas)
            
            if (isEditMode) {
                intent.putExtra("EVENTO_ID", evento?.id)
                intent.putExtra("IS_EDIT_MODE", true)
                // Pasamos los participantes actuales para que no se pierdan
                val listaActual = evento?.distribucion?.flatMap { it.participantes } ?: emptyList()
                intent.putParcelableArrayListExtra("LISTA_PARTICIPANTES", ArrayList(listaActual))
            }
            
            startActivity(intent)
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun cargarDatosDelEvento(evento: Evento) {
        nombreEventoEditText.setText(evento.nombre)
        ubicacionEditText.setText(evento.ubicacion ?: "")
        telefonoEditText.setText(evento.telefono?.toString() ?: "")
        
        // LÓGICA DE RECUPERACIÓN DE PARTICIPANTES:
        // Si num_participantes es null o 0, sumamos los que hay en las mesas
        val totalParticipantes = if (evento.num_participantes != null && evento.num_participantes > 0) {
            evento.num_participantes
        } else {
            evento.distribucion.sumOf { it.participantes.size }
        }
        
        participantesEditText.setText(totalParticipantes.toString())
        mesasEditText.setText(evento.distribucion.size.toString())

        if (!evento.fecha.isNullOrEmpty()) {
            val dateStr = evento.fecha
            fechaIsoParaNube = dateStr
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            try {
                val parsedDate = parser.parse(dateStr)
                fechaEditText.setText(parsedDate?.let { formatter.format(it) } ?: dateStr)
            } catch (e: Exception) {
                fechaEditText.setText(dateStr)
            }
        }
    }
}
