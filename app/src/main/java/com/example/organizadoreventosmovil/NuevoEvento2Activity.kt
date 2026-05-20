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
import com.google.android.material.textfield.TextInputLayout

class NuevoEvento2Activity : AppCompatActivity() {

    private val participantes = mutableListOf<Participante>()
    private lateinit var adapter: ParticipanteAdapter
    private lateinit var tvCounter: TextView
    
    private lateinit var etNombre: TextInputEditText
    private lateinit var etPrefiere: TextInputEditText
    private lateinit var etNoPrefiere: TextInputEditText
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
        
        val tilPrefiere = findViewById<TextInputLayout>(R.id.tilPrefiere)
        val tilNoPrefiere = findViewById<TextInputLayout>(R.id.tilNoPrefiere)
        
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

        adapter = ParticipanteAdapter(
            participantes,
            onEliminarClick = { participante ->
                participantes.remove(participante)
                adapter.notifyDataSetChanged()
                actualizarContador()
                
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

        // Configurar selectores
        etPrefiere.setOnClickListener { mostrarSelectorParticipantes(etPrefiere) }
        etNoPrefiere.setOnClickListener { mostrarSelectorParticipantes(etNoPrefiere) }
        
        tilPrefiere.setEndIconOnClickListener { mostrarSelectorParticipantes(etPrefiere) }
        tilNoPrefiere.setEndIconOnClickListener { mostrarSelectorParticipantes(etNoPrefiere) }

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

    private fun mostrarSelectorParticipantes(targetEditText: TextInputEditText) {
        val nombreActual = etNombre.text.toString().trim()
        
        // REQUISITO: No dejar seleccionar si el nombre está vacío
        if (nombreActual.isEmpty()) {
            Toast.makeText(this, "Escribe primero el nombre del invitado para poder seleccionar sus preferencias", Toast.LENGTH_LONG).show()
            etNombre.requestFocus()
            return
        }

        if (participantes.isEmpty()) {
            Toast.makeText(this, "Añade primero a otros participantes a la lista", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener nombres ya seleccionados en el OTRO campo para excluirlos
        val nombresEnOtroCampo = if (targetEditText == etPrefiere) {
            etNoPrefiere.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            etPrefiere.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

        // Filtramos: 
        // 1. No puede seleccionarse a sí mismo
        // 2. No puede estar en la lista opuesta (si prefiere a Juan, no puede "no preferir" a Juan)
        val nombresDisponibles = participantes
            .filter { it.nombre != nombreActual && !nombresEnOtroCampo.contains(it.nombre) }
            .map { it.nombre }
            .toTypedArray()

        if (nombresDisponibles.isEmpty()) {
            Toast.makeText(this, "No hay otros participantes disponibles para esta selección", Toast.LENGTH_SHORT).show()
            return
        }

        val seleccionadosPreviamente = targetEditText.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val checkedItems = BooleanArray(nombresDisponibles.size) { i ->
            seleccionadosPreviamente.contains(nombresDisponibles[i])
        }

        val seleccionadosActuales = seleccionadosPreviamente.toMutableList()

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Participantes")
            .setMultiChoiceItems(nombresDisponibles, checkedItems) { _, which, isChecked ->
                val nombre = nombresDisponibles[which]
                if (isChecked) {
                    if (!seleccionadosActuales.contains(nombre)) seleccionadosActuales.add(nombre)
                } else {
                    seleccionadosActuales.remove(nombre)
                }
            }
            .setPositiveButton("Aceptar") { _, _ ->
                targetEditText.setText(seleccionadosActuales.joinToString(", "))
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

        if (participantes.any { it.nombre.equals(nombre, ignoreCase = true) }) {
            Toast.makeText(this, "Ya existe un participante con ese nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevoParticipante = Participante(nombre, prefiere, noPrefiere)
        participantes.add(nuevoParticipante)
        adapter.notifyDataSetChanged()
        actualizarContador()

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

        if (participantes.any { it !== participante && it.nombre.equals(nombre, ignoreCase = true) }) {
            Toast.makeText(this, "Ya existe un participante con ese nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val index = participantes.indexOf(participante)
        if (index != -1) {
            participantes[index] = Participante(nombre, prefiere, noPrefiere)
            adapter.notifyDataSetChanged()
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
