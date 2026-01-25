package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.ParticipanteAdapter
import com.example.organizadoreventosmovil.Constructores.Participante
import com.google.android.material.textfield.TextInputEditText

class NuevoEvento2Activity : AppCompatActivity() {

    private val participantes = mutableListOf<Participante>()
    private lateinit var adapter: ParticipanteAdapter
    private lateinit var tvCounter: TextView
    
    private lateinit var etNombre: TextInputEditText
    private lateinit var etPrefiere: AppCompatAutoCompleteTextView
    private lateinit var etNoPrefiere: AppCompatAutoCompleteTextView
    private lateinit var btnAgregar: Button

    private var nombreEvento: String? = null
    private var fechaEvento: String? = null
    private var lugarEvento: String? = null
    private var telefonoEvento: String? = null
    private var numeroMesas = 5
    private var numParticipantesTotal = 0

    private var isEditMode = false
    private var eventoId: String? = null
    
    // Para manejar el modo edición de participante
    private var participanteEnEdicion: Participante? = null

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
        etNombre = findViewById(R.id.etNombreParticipante)
        etPrefiere = findViewById(R.id.etPrefiere)
        etNoPrefiere = findViewById(R.id.etNoPrefiere)
        btnAgregar = findViewById(R.id.btnAgregarParticipante)
        val rvParticipantes = findViewById<RecyclerView>(R.id.rvParticipantes)
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguiente)

        // Cargar participantes si ya vienen del intent (modo edición o vuelta atrás)
        val participantesPrevios = intent.getParcelableArrayListExtra<Participante>("LISTA_PARTICIPANTES")
        if (participantesPrevios != null) {
            participantes.clear()
            participantes.addAll(participantesPrevios)
        }

        actualizarContador()
        actualizarSugerencias()

        adapter = ParticipanteAdapter(
            participantes,
            onEliminarClick = { participante ->
                participantes.remove(participante)
                adapter.notifyDataSetChanged()
                actualizarContador()
                actualizarSugerencias()
                
                // Si estaba editando este participante, cancelar edición
                if (participanteEnEdicion == participante) {
                    cancelarEdicion()
                }
            },
            onEditarClick = { participante ->
                cargarParticipanteParaEdicion(participante)
            }
        )
        rvParticipantes.layoutManager = LinearLayoutManager(this)
        rvParticipantes.adapter = adapter

        btnAgregar.setOnClickListener {
            if (participanteEnEdicion != null) {
                actualizarParticipante()
            } else {
                agregarParticipante()
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

    private fun agregarParticipante() {
        if (participantes.size >= numParticipantesTotal) {
            Toast.makeText(this, "Ya has completado la lista de invitados ($numParticipantesTotal)", Toast.LENGTH_LONG).show()
            return
        }

        val nombre = etNombre.text.toString().trim()
        val prefiere = etPrefiere.text.toString().trim()
        val noPrefiere = etNoPrefiere.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre del invitado es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que el nombre no exista ya
        if (participantes.any { it.nombre.equals(nombre, ignoreCase = true) }) {
            Toast.makeText(this, "Ya existe un participante con ese nombre", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar preferencias (deben existir en la lista)
        val prefiereNombres = prefiere.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val noPrefiereNombres = noPrefiere.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        for (nombrePref in prefiereNombres) {
            if (!participantes.any { it.nombre.equals(nombrePref, ignoreCase = true) }) {
                Toast.makeText(this, "El participante '$nombrePref' no existe. Añádelo primero.", Toast.LENGTH_LONG).show()
                return
            }
        }
        
        for (nombreNoPref in noPrefiereNombres) {
            if (!participantes.any { it.nombre.equals(nombreNoPref, ignoreCase = true) }) {
                Toast.makeText(this, "El participante '$nombreNoPref' no existe. Añádelo primero.", Toast.LENGTH_LONG).show()
                return
            }
        }

        val nuevoParticipante = Participante(nombre, prefiere, noPrefiere)
        participantes.add(nuevoParticipante)
        adapter.notifyDataSetChanged()
        actualizarContador()
        actualizarSugerencias()

        etNombre.text?.clear()
        etPrefiere.text?.clear()
        etNoPrefiere.text?.clear()
        etNombre.requestFocus()
    }

    private fun cargarParticipanteParaEdicion(participante: Participante) {
        participanteEnEdicion = participante
        etNombre.setText(participante.nombre)
        etPrefiere.setText(participante.prefiere)
        etNoPrefiere.setText(participante.noPrefiere)
        btnAgregar.text = "Actualizar"
        etNombre.requestFocus()
    }

    private fun actualizarParticipante() {
        val participante = participanteEnEdicion ?: return
        
        val nombre = etNombre.text.toString().trim()
        val prefiere = etPrefiere.text.toString().trim()
        val noPrefiere = etNoPrefiere.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre del invitado es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que el nombre no exista (excepto si es el mismo)
        if (participantes.any { it !== participante && it.nombre.equals(nombre, ignoreCase = true) }) {
            Toast.makeText(this, "Ya existe un participante con ese nombre", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar preferencias
        val prefiereNombres = prefiere.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val noPrefiereNombres = noPrefiere.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        for (nombrePref in prefiereNombres) {
            if (!participantes.any { it.nombre.equals(nombrePref, ignoreCase = true) }) {
                Toast.makeText(this, "El participante '$nombrePref' no existe. Añádelo primero.", Toast.LENGTH_LONG).show()
                return
            }
        }
        
        for (nombreNoPref in noPrefiereNombres) {
            if (!participantes.any { it.nombre.equals(nombreNoPref, ignoreCase = true) }) {
                Toast.makeText(this, "El participante '$nombreNoPref' no existe. Añádelo primero.", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Actualizar el participante
        val index = participantes.indexOf(participante)
        if (index != -1) {
            participantes[index] = Participante(nombre, prefiere, noPrefiere)
            adapter.notifyDataSetChanged()
            actualizarSugerencias()
        }

        cancelarEdicion()
    }

    private fun cancelarEdicion() {
        participanteEnEdicion = null
        etNombre.text?.clear()
        etPrefiere.text?.clear()
        etNoPrefiere.text?.clear()
        btnAgregar.text = "Agregar Participante"
        etNombre.requestFocus()
    }

    private fun actualizarContador() {
        tvCounter.text = "Invitados: ${participantes.size} / $numParticipantesTotal"
    }
    
    private fun actualizarSugerencias() {
        val nombres = participantes.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nombres)
        etPrefiere.setAdapter(adapter)
        etNoPrefiere.setAdapter(adapter)
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
