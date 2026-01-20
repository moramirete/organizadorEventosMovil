package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.MesaAdapter
import com.example.organizadoreventosmovil.Constructores.Evento
import com.example.organizadoreventosmovil.Constructores.Mesa
import com.example.organizadoreventosmovil.Constructores.Participante
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AsignacionParticipantesActivity : AppCompatActivity() {

    private lateinit var rvMesas: RecyclerView
    private lateinit var adapter: MesaAdapter
    private val mesas = mutableListOf<Mesa>()
    private var todosParticipantes = ArrayList<Participante>()

    private var nombreEvento: String = ""
    private var fechaEvento: String = ""
    private var lugarEvento: String = ""
    private var telefonoEvento: String? = null

    // Para modo edición
    private var isEditMode = false
    private var eventoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignacion_participantes)

        // Recoger datos básicos del intent
        nombreEvento = intent.getStringExtra("NOMBRE_EVENTO") ?: ""
        fechaEvento = intent.getStringExtra("FECHA_EVENTO") ?: ""
        lugarEvento = intent.getStringExtra("LUGAR_EVENTO") ?: ""
        telefonoEvento = intent.getStringExtra("TELEFONO_EVENTO")
        todosParticipantes = intent.getParcelableArrayListExtra("LISTA_PARTICIPANTES") ?: ArrayList()
        val numMesas = intent.getIntExtra("NUMERO_MESAS", 5)

        // Recoger datos de modo edición
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        eventoId = intent.getStringExtra("EVENTO_ID")
        val eventoJson = intent.getStringExtra("EVENTO_JSON")

        if (isEditMode && eventoJson != null) {
            try {
                val eventoOriginal = Json.decodeFromString<Evento>(eventoJson)
                mesas.clear()
                mesas.addAll(eventoOriginal.distribucion)
                // Si el usuario cambió el número de mesas en la pantalla 1, ajustamos
                if (mesas.size != numMesas) {
                    ajustarCantidadDeMesas(numMesas)
                }
            } catch (e: Exception) {
                Log.e("EDIT_MODE", "Error al decodificar distribución original: ${e.message}")
                inicializarMesas(numMesas)
            }
        } else {
            inicializarMesas(numMesas)
        }

        rvMesas = findViewById(R.id.rvMesas)
        findViewById<Button>(R.id.btnQuitarTodos).setOnClickListener {
            mesas.forEach { it.participantes.clear() }
            actualizarAdapter()
        }

        findViewById<Button>(R.id.btnAsignarAuto).setOnClickListener {
            asignarAutomaticamente()
        }

        findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            guardarEventoEnNube()
        }

        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            finish()
        }

        rvMesas.layoutManager = GridLayoutManager(this, 2)
        actualizarAdapter()
    }

    private fun ajustarCantidadDeMesas(nuevaCantidad: Int) {
        if (mesas.size < nuevaCantidad) {
            // Añadir mesas nuevas
            for (i in (mesas.size + 1)..nuevaCantidad) {
                mesas.add(Mesa(numero = i))
            }
        } else if (mesas.size > nuevaCantidad) {
            // Quitar mesas sobrantes
            while (mesas.size > nuevaCantidad) {
                mesas.removeAt(mesas.size - 1)
            }
        }
    }

    private fun asignarAutomaticamente() {
        mesas.forEach { it.participantes.clear() }
        val participantesMezclados = todosParticipantes.shuffled()
        var mesaActual = 0
        for (participante in participantesMezclados) {
            if (mesas[mesaActual].participantes.size < mesas[mesaActual].capacidad) {
                mesas[mesaActual].participantes.add(participante)
            }
            mesaActual = (mesaActual + 1) % mesas.size
        }
        actualizarAdapter()
    }

    private fun guardarEventoEnNube() {
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show()
            return
        }

        val eventoParaGuardar = Evento(
            id = eventoId,
            usuario_id = user.id,
            nombre = nombreEvento,
            fecha = fechaEvento,
            ubicacion = lugarEvento,
            telefono = telefonoEvento?.toIntOrNull(),
            distribucion = mesas
        )

        lifecycleScope.launch {
            try {
                if (isEditMode && eventoId != null) {
                    // MODO EDICIÓN: Actualizar evento existente
                    SupabaseClient.client.postgrest["eventos"].update(eventoParaGuardar) {
                        filter {
                            eq("id", eventoId!!)
                        }
                    }
                    Toast.makeText(this@AsignacionParticipantesActivity, "¡Actualizado con éxito!", Toast.LENGTH_LONG).show()
                } else {
                    // MODO NUEVO: Insertar evento nuevo
                    SupabaseClient.client.postgrest["eventos"].insert(eventoParaGuardar)
                    Toast.makeText(this@AsignacionParticipantesActivity, "¡Guardado con éxito!", Toast.LENGTH_LONG).show()
                }

                // Volver a la pantalla de inicio tras guardar/actualizar
                startActivity(Intent(this@AsignacionParticipantesActivity, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                })
                finish()
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error al guardar/actualizar: ${e.message}")
                Toast.makeText(this@AsignacionParticipantesActivity, "Error en la operación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun inicializarMesas(cantidad: Int) {
        mesas.clear()
        for (i in 1..cantidad) mesas.add(Mesa(numero = i))
    }

    private fun actualizarAdapter() {
        adapter = MesaAdapter(mesas) { mostrarDialogoSeleccionParticipante(it) }
        rvMesas.adapter = adapter
    }

    private fun mostrarDialogoSeleccionParticipante(mesa: Mesa) {
        val asignados = mesas.flatMap { it.participantes }.toSet()
        val disponibles = todosParticipantes.filter { !asignados.contains(it) }
        val nombres = disponibles.map { it.nombre }.toTypedArray()

        if (nombres.isEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("Asignar a Mesa ${mesa.numero}")
            .setItems(nombres) { _, which ->
                mesa.participantes.add(disponibles[which])
                adapter.notifyDataSetChanged()
            }
            .show()
    }
}
