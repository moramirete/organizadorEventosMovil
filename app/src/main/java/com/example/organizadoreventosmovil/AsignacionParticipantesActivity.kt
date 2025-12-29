package com.example.organizadoreventosmovil

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.MesaAdapter
import com.example.organizadoreventosmovil.Constructores.Mesa
import com.example.organizadoreventosmovil.Constructores.Participante

class AsignacionParticipantesActivity : AppCompatActivity() {

    private lateinit var rvMesas: RecyclerView
    private lateinit var adapter: MesaAdapter
    private val mesas = mutableListOf<Mesa>()
    private var todosParticipantes = ArrayList<Participante>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignacion_participantes)

        // Obtener datos del Intent
        todosParticipantes = intent.getParcelableArrayListExtra("LISTA_PARTICIPANTES") ?: ArrayList()
        // Intentamos obtener el número de mesas, si no viene, ponemos 5 por defecto
        val numMesas = intent.getIntExtra("NUMERO_MESAS", 5)

        // Inicializar Mesas
        inicializarMesas(numMesas)

        rvMesas = findViewById(R.id.rvMesas)
        val btnAsignarAuto = findViewById<Button>(R.id.btnAsignarAuto)
        val btnQuitarTodos = findViewById<Button>(R.id.btnQuitarTodos)
        val btnReiniciar = findViewById<Button>(R.id.btnReiniciar)
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        // Configurar RecyclerView con Grid (2 columnas)
        rvMesas.layoutManager = GridLayoutManager(this, 2)
        actualizarAdapter()

        // Botón Asignar Automáticamente
        btnAsignarAuto.setOnClickListener {
            asignarAutomaticamente()
        }

        // Botón Quitar Todos (Vaciar mesas)
        btnQuitarTodos.setOnClickListener {
            mesas.forEach { it.participantes.clear() }
            actualizarAdapter()
            Toast.makeText(this, "Mesas vaciadas", Toast.LENGTH_SHORT).show()
        }

        // Botón Reiniciar (Vaciar y restaurar estado inicial)
        btnReiniciar.setOnClickListener {
            inicializarMesas(numMesas)
            actualizarAdapter()
            Toast.makeText(this, "Reiniciado", Toast.LENGTH_SHORT).show()
        }

        btnVolver.setOnClickListener { finish() }

        btnGuardar.setOnClickListener {
            Toast.makeText(this, "Distribución guardada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inicializarMesas(cantidad: Int) {
        mesas.clear()
        for (i in 1..cantidad) {
            mesas.add(Mesa(numero = i))
        }
    }

    private fun actualizarAdapter() {
        adapter = MesaAdapter(mesas) { mesa ->
            mostrarDialogoSeleccionParticipante(mesa)
        }
        rvMesas.adapter = adapter
    }

    private fun mostrarDialogoSeleccionParticipante(mesa: Mesa) {
        // Filtramos los participantes que YA están asignados a alguna mesa
        val asignados = mesas.flatMap { it.participantes }.toSet()
        val disponibles = todosParticipantes.filter { !asignados.contains(it) }

        if (disponibles.isEmpty()) {
            Toast.makeText(this, "No hay participantes disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = disponibles.map { it.nombre }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Agregar a Mesa ${mesa.numero}")
            .setItems(nombres) { _, which ->
                val seleccionado = disponibles[which]
                mesa.participantes.add(seleccionado)
                adapter.notifyDataSetChanged() // Refrescamos la vista
                Toast.makeText(this, "${seleccionado.nombre} agregado a Mesa ${mesa.numero}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun asignarAutomaticamente() {
        // Limpiamos asignaciones actuales
        mesas.forEach { it.participantes.clear() }

        // Algoritmo simple de reparto (Round Robin)
        // Se puede mejorar considerando preferencias
        var mesaIndex = 0
        for (participante in todosParticipantes) {
            mesas[mesaIndex].participantes.add(participante)
            mesaIndex = (mesaIndex + 1) % mesas.size
        }

        actualizarAdapter()
        Toast.makeText(this, "Asignación automática completada", Toast.LENGTH_SHORT).show()
    }
}
