package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.ParticipanteAdapter
import com.example.organizadoreventosmovil.Constructores.Participante
import com.google.android.material.textfield.TextInputEditText

class NuevoEvento2Activity : AppCompatActivity() {

    private val participantes = mutableListOf<Participante>()
    private lateinit var adapter: ParticipanteAdapter
    private lateinit var tvCounter: TextView

    private var nombreEvento: String? = null
    private var fechaEvento: String? = null
    private var lugarEvento: String? = null
    private var telefonoEvento: String? = null
    private var numeroMesas = 5
    private var numParticipantesTotal = 0

    private var isEditMode = false
    private var eventoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento2)

        nombreEvento = intent.getStringExtra("NOMBRE_EVENTO")
        fechaEvento = intent.getStringExtra("FECHA_EVENTO")
        lugarEvento = intent.getStringExtra("LUGAR_EVENTO")
        telefonoEvento = intent.getStringExtra("TELEFONO_EVENTO")
        numeroMesas = intent.getIntExtra("NUMERO_MESAS", 5)
        numParticipantesTotal = intent.getIntExtra("NUM_PARTICIPANTES", 0)

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        eventoId = intent.getStringExtra("EVENTO_ID")

        tvCounter = findViewById(R.id.tvCounter)

        // Cargar participantes si ya vienen del intent (modo edición o vuelta atrás)
        val participantesPrevios = intent.getParcelableArrayListExtra<Participante>("LISTA_PARTICIPANTES")
        if (participantesPrevios != null) {
            participantes.clear()
            participantes.addAll(participantesPrevios)
        }

        actualizarContador()

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreParticipante)
        val etPrefiere = findViewById<TextInputEditText>(R.id.etPrefiere)
        val etNoPrefiere = findViewById<TextInputEditText>(R.id.etNoPrefiere)
        val btnAgregar = findViewById<Button>(R.id.btnAgregarParticipante)
        val rvParticipantes = findViewById<RecyclerView>(R.id.rvParticipantes)
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguiente)

        adapter = ParticipanteAdapter(participantes) { participante ->
            participantes.remove(participante)
            adapter.notifyDataSetChanged()
            actualizarContador()
        }
        rvParticipantes.layoutManager = LinearLayoutManager(this)
        rvParticipantes.adapter = adapter

        btnAgregar.setOnClickListener {
            if (participantes.size >= numParticipantesTotal) {
                Toast.makeText(this, "Ya has completado la lista de invitados ($numParticipantesTotal)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val nombre = etNombre.text.toString().trim()
            val prefiere = etPrefiere.text.toString().trim()
            val noPrefiere = etNoPrefiere.text.toString().trim()

            if (nombre.isNotEmpty()) {
                val nuevoParticipante = Participante(nombre, prefiere, noPrefiere)
                participantes.add(nuevoParticipante)
                adapter.notifyDataSetChanged()
                actualizarContador()

                etNombre.text?.clear()
                etPrefiere.text?.clear()
                etNoPrefiere.text?.clear()
                etNombre.requestFocus()
            } else {
                Toast.makeText(this, "El nombre del invitado es obligatorio", Toast.LENGTH_SHORT).show()
            }
        }

        btnVolver.setOnClickListener { finish() }

        btnSiguiente.setOnClickListener {
            if (participantes.size < numParticipantesTotal) {
                AlertDialog.Builder(this)
                    .setTitle("Invitados incompletos")
                    .setMessage("Has indicado que el evento tiene $numParticipantesTotal invitados, pero solo has añadido a ${participantes.size}. Si continúas ahora, los datos no se guardarán correctamente. ¿Deseas continuar de todas formas?")
                    .setPositiveButton("Continuar") { _, _ -> avanzar() }
                    .setNegativeButton("Añadir más", null)
                    .show()
            } else {
                avanzar()
            }
        }
    }

    private fun actualizarContador() {
        tvCounter.text = "Invitados: ${participantes.size} / $numParticipantesTotal"
    }

    private fun avanzar() {
        val intent = Intent(this, AsignacionParticipantesActivity::class.java)
        intent.putExtra("NOMBRE_EVENTO", nombreEvento)
        intent.putExtra("FECHA_EVENTO", fechaEvento)
        intent.putExtra("LUGAR_EVENTO", lugarEvento)
        intent.putExtra("TELEFONO_EVENTO", telefonoEvento)
        intent.putExtra("NUMERO_MESAS", numeroMesas)
        intent.putExtra("NUM_PARTICIPANTES", numParticipantesTotal)
        intent.putParcelableArrayListExtra("LISTA_PARTICIPANTES", ArrayList(participantes))

        if (isEditMode) {
            intent.putExtra("EVENTO_ID", eventoId)
            intent.putExtra("IS_EDIT_MODE", true)
        }
        startActivity(intent)
    }
}
