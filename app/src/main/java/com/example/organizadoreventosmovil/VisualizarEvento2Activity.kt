package com.example.organizadoreventosmovil

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.MesaAdapter
import com.example.organizadoreventosmovil.Constructores.Evento
import com.example.organizadoreventosmovil.Constructores.Mesa
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class VisualizarEvento2Activity : AppCompatActivity() {

    private lateinit var mesasRecyclerView: RecyclerView
    private lateinit var mesaAdapter: MesaAdapter
    private var evento: Evento? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_evento2)

        val headerTitle = findViewById<TextView>(R.id.headerTitle)
        val btnBack = findViewById<Button>(R.id.btnBack)
        mesasRecyclerView = findViewById(R.id.mesasRecyclerView)

        val eventoJson = intent.getStringExtra("EVENTO_JSON")

        if (eventoJson != null) {
            try {
                evento = Json.decodeFromString<Evento>(eventoJson)
            } catch (e: Exception) {
                // Manejar error
            }
        }

        evento?.let {
            headerTitle.text = it.nombre
            setupRecyclerView(it.distribucion)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView(mesas: List<Mesa>) {
        mesaAdapter = MesaAdapter(mesas.toMutableList()) { mesa ->
            mostrarDialogoParticipantes(mesa)
        }
        mesasRecyclerView.layoutManager = GridLayoutManager(this, 2)
        mesasRecyclerView.adapter = mesaAdapter
    }

    private fun mostrarDialogoParticipantes(mesa: Mesa) {
        val nombres = if (mesa.participantes.isEmpty()) {
            "No hay invitados asignados a esta mesa."
        } else {
            mesa.participantes.joinToString("\n") { "• ${it.nombre}" }
        }

        AlertDialog.Builder(this)
            .setTitle("Invitados - Mesa ${mesa.numero}")
            .setMessage(nombres)
            .setPositiveButton("Cerrar", null)
            .show()
    }
}
