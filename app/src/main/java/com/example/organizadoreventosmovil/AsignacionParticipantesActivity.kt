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
        // Vaciar participantes de las mesas
        mesas.forEach { it.participantes.clear() }

        // 1. Mapear y normalizar los participantes (a minúsculas y listas de amigos/enemigos)
        class PersonaAlg(
            val original: Participante,
            val nombreCanonico: String,
            val amigos: List<String>,
            val enemigos: List<String>
        )

        val personas = todosParticipantes.map { p ->
            val amigos = p.prefiere.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            val enemigos = p.noPrefiere.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            PersonaAlg(p, p.nombre.trim().lowercase(), amigos, enemigos)
        }

        // 2. Ordenar por peso = amigos.size + enemigos.size (quien tiene más restricciones va primero)
        val prioritarios = personas.sortedByDescending { it.amigos.size + it.enemigos.size }

        val excluidos = mutableListOf<Participante>()

        // 3. Algoritmo Greedy con Heurística de Satisfacción (Idéntico a Python)
        for (p in prioritarios) {
            var mejorMesa: Mesa? = null
            var mejorPuntuacion = -1000 // Puntuación inicial igual que en Python

            for (mesa in mesas) {
                // A. Comprobar Hard Constraints
                // 1. Capacidad
                if (mesa.participantes.size >= mesa.capacidad) {
                    continue
                }

                // 2. Enemistades (HARD) - Si p tiene a alguien de la mesa como enemigo, o alguien en la mesa tiene a p como enemigo
                var esViable = true
                for (otro in mesa.participantes) {
                    val otroCanon = otro.nombre.trim().lowercase()
                    val otroPersonaAlg = personas.find { it.nombreCanonico == otroCanon }
                    val pEsEnemigoDeOtro = otroPersonaAlg?.enemigos?.contains(p.nombreCanonico) == true
                    val otroEsEnemigoDeP = p.enemigos.contains(otroCanon)

                    if (otroEsEnemigoDeP || pEsEnemigoDeOtro) {
                        esViable = false
                        break
                    }
                }
                if (!esViable) {
                    continue
                }

                // B. Calcular Satisfacción (SOFT)
                var score = 0
                for (otro in mesa.participantes) {
                    val otroCanon = otro.nombre.trim().lowercase()
                    val otroPersonaAlg = personas.find { it.nombreCanonico == otroCanon }

                    // Si p quiere estar con 'otro'
                    if (p.amigos.contains(otroCanon)) {
                        score += 10
                    }
                    // Si 'otro' quiere estar con p
                    if (otroPersonaAlg?.amigos?.contains(p.nombreCanonico) == true) {
                        score += 10
                    }
                }

                if (score > mejorPuntuacion) {
                    mejorPuntuacion = score
                    mejorMesa = mesa
                }
            }

            if (mejorMesa != null) {
                mejorMesa.participantes.add(p.original)
            } else {
                excluidos.add(p.original)
            }
        }

        actualizarUI()

        if (excluidos.isNotEmpty()) {
            val nombresExcluidos = excluidos.joinToString(", ") { it.nombre }
            Toast.makeText(this, "Asignación inteligente completada. Excluidos: $nombresExcluidos", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Asignación inteligente completada con éxito", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calcularPuntuacionDeMesa(p: Participante, mesa: Mesa): Int {
        val pAmigos = p.prefiere.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val pEnemigos = p.noPrefiere.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

        var score = -(mesa.participantes.size * 5)
        for (otro in mesa.participantes) {
            val otroCanon = otro.nombre.trim().lowercase()
            val otroAmigos = otro.prefiere.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            val otroEnemigos = otro.noPrefiere.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

            if (pEnemigos.contains(otroCanon) || otroEnemigos.contains(p.nombre.trim().lowercase())) {
                score -= 1000
            }
            if (pAmigos.contains(otroCanon) || otroAmigos.contains(p.nombre.trim().lowercase())) {
                score += 100
            }
        }
        return score
    }

    private fun detectarConflictosGraves() = mesas.any { mesa ->
        mesa.participantes.any { p ->
            val enemigos = p.noPrefiere.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            mesa.participantes.any { otro ->
                enemigos.contains(otro.nombre.trim().lowercase())
            }
        }
    }

    private fun obtenerListaConflictos(): List<String> {
        val lista = mutableListOf<String>()
        for (mesa in mesas) {
            for (p in mesa.participantes) {
                // Parseamos los enemigos de p
                val enemigos = p.noPrefiere.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                for (enemigoNombre in enemigos) {
                    val enemigoEnMesa = mesa.participantes.find { it.nombre.trim().lowercase() == enemigoNombre }
                    if (enemigoEnMesa != null) {
                        lista.add("❌ Mesa ${mesa.numero}: ${p.nombre} y ${enemigoEnMesa.nombre} se llevan mal.")
                    }
                }

                // Parseamos las preferencias (amigos) de p
                val amigos = p.prefiere.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                for (amigoNombre in amigos) {
                    // Si el amigo existe en la lista de todos los participantes del evento, pero no está en esta mesa
                    val existeAmigoEnEvento = todosParticipantes.any { it.nombre.trim().lowercase() == amigoNombre }
                    val estaAmigoEnMesa = mesa.participantes.any { it.nombre.trim().lowercase() == amigoNombre }
                    if (existeAmigoEnEvento && !estaAmigoEnMesa) {
                        // Buscamos el nombre original del amigo para que quede bonito en el mensaje
                        val amigoOriginal = todosParticipantes.find { it.nombre.trim().lowercase() == amigoNombre }?.nombre ?: amigoNombre
                        lista.add("⚠️ ${p.nombre} no está sentado con $amigoOriginal.")
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
            hora = "00:00:00",
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
