package com.example.organizadoreventosmovil

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AsignacionParticipantesActivity : AppCompatActivity() {

    private lateinit var rvMesas: RecyclerView
    private lateinit var adapter: MesaAdapter
    private lateinit var btnVerConflictos: MaterialButton
    private val mesas = mutableListOf<Mesa>()
    private var todosParticipantes = ArrayList<Participante>()

    private var nombreEvento: String = ""
    private var fechaEvento: String = ""
    private var lugarEvento: String = ""
    private var telefonoEvento: String? = null
    private var numParticipantesTotal = 0

    private var isEditMode = false
    private var eventoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignacion_participantes)

        nombreEvento = intent.getStringExtra("NOMBRE_EVENTO") ?: ""
        fechaEvento = intent.getStringExtra("FECHA_EVENTO") ?: ""
        lugarEvento = intent.getStringExtra("LUGAR_EVENTO") ?: ""
        telefonoEvento = intent.getStringExtra("TELEFONO_EVENTO")
        todosParticipantes = intent.getParcelableArrayListExtra("LISTA_PARTICIPANTES") ?: ArrayList()
        val numMesas = intent.getIntExtra("NUMERO_MESAS", 5)
        numParticipantesTotal = intent.getIntExtra("NUM_PARTICIPANTES", 0)

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        eventoId = intent.getStringExtra("EVENTO_ID")
        val eventoJson = intent.getStringExtra("EVENTO_JSON")

        if (isEditMode && eventoJson != null) {
            try {
                val eventoOriginal = Json.decodeFromString<Evento>(eventoJson)
                mesas.clear()
                mesas.addAll(eventoOriginal.distribucion)
                if (mesas.size != numMesas) ajustarCantidadDeMesas(numMesas)
                else recalcularCapacidades()
            } catch (e: Exception) { inicializarMesas(numMesas) }
        } else {
            inicializarMesas(numMesas)
        }

        rvMesas = findViewById(R.id.rvMesas)
        btnVerConflictos = findViewById(R.id.btnVerConflictos)
        btnVerConflictos.setOnClickListener { mostrarDialogoConflictos() }

        findViewById<Button>(R.id.btnQuitarTodos).setOnClickListener {
            mesas.forEach { it.participantes.clear() }
            actualizarUI()
        }

        findViewById<Button>(R.id.btnAsignarAuto).setOnClickListener { asignarAlgoritmoOptimo() }

        findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            if (detectarConflictosGraves()) {
                AlertDialog.Builder(this)
                    .setTitle("Conflictos Críticos")
                    .setMessage("Hay personas que no se llevan bien sentadas juntas. ¿Guardar de todas formas?")
                    .setPositiveButton("Guardar") { _, _ -> guardarEventoEnNube() }
                    .setNegativeButton("Corregir", null)
                    .show()
            } else {
                guardarEventoEnNube()
            }
        }

        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }
        rvMesas.layoutManager = GridLayoutManager(this, 2)
        actualizarUI()
    }

    private fun inicializarMesas(cantidad: Int) {
        mesas.clear()
        if (cantidad <= 0) return
        val totalReal = todosParticipantes.size
        val baseCapacidad = totalReal / cantidad
        val resto = totalReal % cantidad
        for (i in 1..cantidad) {
            val capacidadMesa = if (i <= resto) baseCapacidad + 1 else baseCapacidad
            mesas.add(Mesa(numero = i, capacidad = capacidadMesa))
        }
    }

    private fun recalcularCapacidades() {
        if (mesas.isEmpty()) return
        val totalReal = todosParticipantes.size
        val baseCapacidad = totalReal / mesas.size
        val resto = totalReal % mesas.size
        for (i in mesas.indices) {
            mesas[i].capacidad = if (i < resto) baseCapacidad + 1 else baseCapacidad
        }
    }

    private fun ajustarCantidadDeMesas(nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) return
        if (mesas.size < nuevaCantidad) {
            for (i in (mesas.size + 1)..nuevaCantidad) mesas.add(Mesa(numero = i))
        } else if (mesas.size > nuevaCantidad) {
            while (mesas.size > nuevaCantidad) mesas.removeAt(mesas.size - 1)
        }
        recalcularCapacidades()
    }

    private fun asignarAlgoritmoOptimo() {
        mesas.forEach { it.participantes.clear() }
        val prioritarios = todosParticipantes.sortedByDescending { 
            (if (it.prefiere.isNotEmpty()) 2 else 0) + (if (it.noPrefiere.isNotEmpty()) 3 else 0)
        }
        for (participante in prioritarios) {
            var mejorMesa: Mesa? = null
            var mejorPuntuacion = -999999
            for (mesa in mesas) {
                if (mesa.participantes.size < mesa.capacidad) {
                    val score = calcularPuntuacionDeMesa(participante, mesa)
                    if (score > mejorPuntuacion) {
                        mejorPuntuacion = score
                        mejorMesa = mesa
                    }
                }
            }
            mejorMesa?.participantes?.add(participante)
        }
        actualizarUI()
        Toast.makeText(this, "Asignación inteligente completada", Toast.LENGTH_SHORT).show()
    }

    private fun calcularPuntuacionDeMesa(p: Participante, mesa: Mesa): Int {
        var score = -(mesa.participantes.size * 5)
        for (otro in mesa.participantes) {
            if (p.noPrefiere.equals(otro.nombre, true) || otro.noPrefiere.equals(p.nombre, true)) score -= 1000
            if (p.prefiere.equals(otro.nombre, true) || otro.prefiere.equals(p.nombre, true)) score += 100
        }
        return score
    }

    private fun detectarConflictosGraves() = mesas.any { mesa ->
        mesa.participantes.any { p -> mesa.participantes.any { otro -> p.noPrefiere.equals(otro.nombre, true) } }
    }

    private fun obtenerListaConflictos(): List<String> {
        val lista = mutableListOf<String>()
        for (mesa in mesas) {
            for (p in mesa.participantes) {
                mesa.participantes.find { it.nombre.equals(p.noPrefiere, true) }?.let {
                    lista.add("❌ Mesa ${mesa.numero}: ${p.nombre} y ${it.nombre} se llevan mal.")
                }
                if (p.prefiere.isNotEmpty()) {
                    if (!mesa.participantes.any { it.nombre.equals(p.prefiere, true) } && todosParticipantes.any { it.nombre.equals(p.prefiere, true) }) {
                        lista.add("⚠️ ${p.nombre} no está sentado con ${p.prefiere}.")
                    }
                }
            }
        }
        return lista.distinct()
    }

    private fun mostrarDialogoConflictos() {
        val errores = obtenerListaConflictos()
        AlertDialog.Builder(this)
            .setTitle("Estado de la Asignación")
            .setMessage(if (errores.isEmpty()) "✅ ¡Perfecto! Se cumplen todas las condiciones." else errores.joinToString("\n"))
            .setPositiveButton("Entendido", null).show()
    }

    private fun actualizarUI() {
        adapter = MesaAdapter(mesas) { mostrarDialogoDetalleMesa(it) }
        rvMesas.adapter = adapter
        val conflictos = obtenerListaConflictos()
        btnVerConflictos.visibility = if (conflictos.isNotEmpty()) View.VISIBLE else View.GONE
        if (conflictos.isNotEmpty()) btnVerConflictos.iconTint = ColorStateList.valueOf(if (detectarConflictosGraves()) Color.RED else Color.parseColor("#FFA500"))
    }
    
    private fun mostrarDialogoDetalleMesa(mesa: Mesa) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_detalle_mesa, null)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloMesa)
        val etCapacidad = dialogView.findViewById<android.widget.EditText>(R.id.etCapacidad)
        val rvParticipantes = dialogView.findViewById<RecyclerView>(R.id.rvParticipantesMesa)
        val btnAgregar = dialogView.findViewById<Button>(R.id.btnAgregarInvitado)
        val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrarDialogo)

        tvTitulo.text = "Mesa ${mesa.numero}"
        etCapacidad.setText(mesa.capacidad.toString())

        val participantesAdapter = com.example.organizadoreventosmovil.Adapters.ParticipanteAdapter(
            mesa.participantes,
            onEliminarClick = { participante ->
                mesa.participantes.remove(participante)
                actualizarUI()
                rvParticipantes.adapter?.notifyDataSetChanged()
            },
            onEditarClick = null  // No edit mode in table dialog
        )
        rvParticipantes.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvParticipantes.adapter = participantesAdapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnAgregar.setOnClickListener {
            mostrarSelectorParticipantes(mesa) {
                participantesAdapter.notifyDataSetChanged()
            }
        }

        btnCerrar.setOnClickListener {
            val nuevaCapacidadTexto = etCapacidad.text.toString()
            val nuevaCapacidad = nuevaCapacidadTexto.toIntOrNull()
            
            if (nuevaCapacidad == null || nuevaCapacidad <= 0) {
                Toast.makeText(this, "La capacidad debe ser un número positivo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (nuevaCapacidad < mesa.participantes.size) {
                Toast.makeText(this, "La capacidad no puede ser menor que los participantes actuales (${mesa.participantes.size})", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            mesa.capacidad = nuevaCapacidad
            dialog.dismiss()
            actualizarUI()
        }

        dialog.show()
    }

    private fun mostrarSelectorParticipantes(mesa: Mesa, onParticipanteAgregado: () -> Unit) {
        val asignados = mesas.flatMap { it.participantes }.toSet()
        val disponibles = todosParticipantes.filter { !asignados.contains(it) }
        val nombres = disponibles.map { it.nombre }.toTypedArray()

        if (nombres.isEmpty()) {
            Toast.makeText(this, "No hay participantes disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Agregar a Mesa ${mesa.numero}")
            .setItems(nombres) { _, which ->
                if (mesa.participantes.size < mesa.capacidad) {
                    mesa.participantes.add(disponibles[which])
                    actualizarUI()
                    onParticipanteAgregado()
                } else {
                    Toast.makeText(this, "Mesa llena. Aumente la capacidad primero.", Toast.LENGTH_LONG).show()
                }
            }
            .show()
    }

    private fun guardarEventoEnNube() {
        // Validar participantes no asignados
        val asignados = mesas.flatMap { it.participantes }.toSet()
        val noAsignados = todosParticipantes.filter { !asignados.contains(it) }
        
        if (noAsignados.isNotEmpty()) {
            val nombresNoAsignados = noAsignados.joinToString("\n") { "• ${it.nombre}" }
            AlertDialog.Builder(this)
                .setTitle("Participantes sin asignar")
                .setMessage("Los siguientes participantes no han sido asignados a ninguna mesa:\n\n$nombresNoAsignados\n\n¿Deseas eliminarlos de la lista de invitados y continuar?")
                .setPositiveButton("Eliminar y guardar") { _, _ ->
                    // Eliminar no asignados de la lista principal
                    todosParticipantes.removeAll(noAsignados)
                    procederConGuardado()
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }
        
        procederConGuardado()
    }
    
    private fun procederConGuardado() {
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user == null) {
            Toast.makeText(this, "Debe iniciar sesión para guardar", Toast.LENGTH_LONG).show()
            return
        }

        // Si el eventoId está vacío, lo tratamos como null para que Supabase genere uno nuevo
        val cleanEventoId = if (eventoId.isNullOrEmpty()) null else eventoId

        val eventoParaGuardar = Evento(
            id = cleanEventoId,
            usuario_id = user.id,
            nombre = nombreEvento,
            fecha = fechaEvento,
            ubicacion = lugarEvento,
            telefono = telefonoEvento?.toLongOrNull(),
            num_participantes = numParticipantesTotal,
            distribucion = mesas
        )

        lifecycleScope.launch {
            try {
                if (isEditMode && cleanEventoId != null) {
                    SupabaseClient.client.postgrest["eventos"].update(eventoParaGuardar) {
                        filter { eq("id", cleanEventoId) }
                    }
                    Toast.makeText(this@AsignacionParticipantesActivity, "Evento actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    SupabaseClient.client.postgrest["eventos"].insert(eventoParaGuardar)
                    Toast.makeText(this@AsignacionParticipantesActivity, "Evento guardado", Toast.LENGTH_SHORT).show()
                }
                startActivity(Intent(this@AsignacionParticipantesActivity, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                })
                finish()
            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Error al guardar: ${e.message}", e)
                Toast.makeText(this@AsignacionParticipantesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
