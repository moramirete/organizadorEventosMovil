package com.example.organizadoreventosmovil

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.MesaAdapter
import com.example.organizadoreventosmovil.Constructores.Mesa
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class VisualizarEvento2Activity : AppCompatActivity() {

    private lateinit var mesasRecyclerView: RecyclerView
    private lateinit var mesaAdapter: MesaAdapter
    private var mesas = mutableListOf<Mesa>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_evento2)

        val headerTitle = findViewById<TextView>(R.id.headerTitle)
        val btnBack = findViewById<Button>(R.id.btnBack)
        mesasRecyclerView = findViewById(R.id.mesasRecyclerView)

        // Recibir datos del Intent
        val nombreEvento = intent.getStringExtra("NOMBRE_EVENTO")
        val distribucionJson = intent.getStringExtra("DISTRIBUCION_MESAS")

        headerTitle.text = nombreEvento ?: "Detalles del Evento"

        if (distribucionJson != null) {
            try {
                // Convertir el JSON de nuevo a una lista de Mesas
                mesas = Json.decodeFromString<MutableList<Mesa>>(distribucionJson)
            } catch (e: Exception) {
                // Manejar error de deserialización si ocurre
            }
        }

        setupRecyclerView()

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        // El adapter para visualizar no necesita lógica de clics
        mesaAdapter = MesaAdapter(mesas) { /* No hacer nada al hacer clic */ }
        mesasRecyclerView.layoutManager = GridLayoutManager(this, 2) // Mostramos en 2 columnas
        mesasRecyclerView.adapter = mesaAdapter
    }
}
