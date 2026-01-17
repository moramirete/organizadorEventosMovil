package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.MesaAdapter
import com.example.organizadoreventosmovil.Constructores.Evento
import com.example.organizadoreventosmovil.Constructores.Mesa
import com.example.organizadoreventosmovil.Constructores.Participante

class AsignacionParticipantesActivity : AppCompatActivity() {

    private lateinit var rvMesas: RecyclerView
    private lateinit var adapter: MesaAdapter
    private val mesas = mutableListOf<Mesa>()
    private var todosParticipantes = ArrayList<Participante>()

    private var nombreEvento: String = ""
    private var fechaEvento: String = ""
    private var lugarEvento: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignacion_participantes)

        nombreEvento = intent.getStringExtra("NOMBRE_EVENTO") ?: ""
        fechaEvento = intent.getStringExtra("FECHA_EVENTO") ?: ""
        lugarEvento = intent.getStringExtra("LUGAR_EVENTO") ?: ""
        todosParticipantes = intent.getParcelableArrayListExtra("LISTA_PARTICIPANTES") ?: ArrayList()
        val numMesas = intent.getIntExtra("NUMERO_MESAS", 5)

        inicializarMesas(numMesas)

        rvMesas = findViewById(R.id.rvMesas)
        findViewById<Button>(R.id.btnQuitarTodos).setOnClickListener {
            mesas.forEach { it.participantes.clear() }
            actualizarAdapter()
            Toast.makeText(this, "Mesas vaciadas", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnReiniciar).setOnClickListener {
            inicializarMesas(numMesas)
            actualizarAdapter()
            Toast.makeText(this, "Reiniciado", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnAsignarAuto).setOnClickListener {
            asignarAutomaticamente()
        }

        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            guardarEventoEnMemoria()
        }

        rvMesas.layoutManager = GridLayoutManager(this, 2)
        actualizarAdapter()
    }

    private fun asignarAutomaticamente() {
        // 1. Vaciar todas las mesas primero
        mesas.forEach { it.participantes.clear() }

        // 2. Mezclar los participantes para una distribución más aleatoria
        val participantesMezclados = todosParticipantes.shuffled()

        // 3. Asignar participantes a las mesas de forma secuencial
        var mesaActual = 0
        for (participante in participantesMezclados) {
            // Buscamos una mesa que no esté llena
            var asignado = false
            while (!asignado) {
                if (mesas[mesaActual].participantes.size < mesas[mesaActual].capacidad) {
                    mesas[mesaActual].participantes.add(participante)
                    asignado = true
                }
                // Pasamos a la siguiente mesa (y volvemos al principio si llegamos al final)
                mesaActual = (mesaActual + 1) % mesas.size
            }
        }

        // 4. Actualizar la pantalla para mostrar el resultado
        actualizarAdapter()
        Toast.makeText(this, "Participantes asignados automáticamente", Toast.LENGTH_SHORT).show()
    }

    private fun guardarEventoEnMemoria() {
        val eventoParaGuardar = Evento(
            nombre = nombreEvento,
            fecha = fechaEvento,
            lugar = lugarEvento,
            distribucion = mesas
        )

        EventoRepository.addEvento(eventoParaGuardar)
        Toast.makeText(this, "Evento guardado localmente", Toast.LENGTH_LONG).show()

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
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
        val asignados = mesas.flatMap { it.participantes }.toSet()
        val disponibles = todosParticipantes.filter { !asignados.contains(it) }

        if (disponibles.isEmpty()) {
            Toast.makeText(this, "No hay más participantes por asignar", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = disponibles.map { it.nombre }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Asignar a Mesa ${mesa.numero}")
            .setItems(nombres) { _, which ->
                val seleccionado = disponibles[which]
                if (mesa.participantes.size < mesa.capacidad) {
                    mesa.participantes.add(seleccionado)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "La mesa está llena", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
