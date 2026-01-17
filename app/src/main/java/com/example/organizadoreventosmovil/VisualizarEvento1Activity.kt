package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.EventoAdapter
import com.example.organizadoreventosmovil.Constructores.Evento
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class VisualizarEvento1Activity : AppCompatActivity() {

    private lateinit var eventosRecyclerView: RecyclerView
    private lateinit var eventoAdapter: EventoAdapter
    private var eventos = mutableListOf<Evento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_evento1)

        eventosRecyclerView = findViewById(R.id.eventosRecyclerView)
        val btnBack = findViewById<Button>(R.id.btnBack)

        setupRecyclerView()

        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Cargar los eventos desde nuestro repositorio en memoria
        cargarEventosDesdeMemoria()
    }

    private fun setupRecyclerView() {
        eventoAdapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, VisualizarEvento2Activity::class.java)
            
            // Pasamos los datos del evento a la siguiente pantalla
            val distribucionJson = Json.encodeToString(evento.distribucion)
            intent.putExtra("NOMBRE_EVENTO", evento.nombre)
            intent.putExtra("DISTRIBUCION_MESAS", distribucionJson)
            
            startActivity(intent)
        }
        eventosRecyclerView.layoutManager = LinearLayoutManager(this)
        eventosRecyclerView.adapter = eventoAdapter
    }

    private fun cargarEventosDesdeMemoria() {
        // Obtenemos la lista de eventos desde nuestro repositorio local
        val eventosGuardados = EventoRepository.getEventos()
        
        // Actualizamos la lista y notificamos al adapter
        eventos.clear()
        eventos.addAll(eventosGuardados)
        eventoAdapter.notifyDataSetChanged()
    }
}
