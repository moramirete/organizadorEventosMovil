package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.EventoAdapter
import com.example.organizadoreventosmovil.Constructores.Evento

class VisualizarEvento1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_evento1)

        val eventosRecyclerView: RecyclerView = findViewById(R.id.eventosRecyclerView)
        val btnBack: Button = findViewById(R.id.btnBack)

        val eventos = listOf(
            Evento("Boda de Ana y Juan", "25/12/2024", "Salón Imperial"),
            Evento("Cumpleaños de Carlos", "15/01/2025", "Casa del Lago"),
            Evento("Conferencia de Tecnología", "05/03/2025", "Centro de Convenciones")
        )

        val adapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, VisualizarEvento2Activity::class.java)
            // Opcional: pasar datos del evento a la siguiente actividad
            // intent.putExtra("NOMBRE_EVENTO", evento.nombre)
            startActivity(intent)
        }

        eventosRecyclerView.layoutManager = LinearLayoutManager(this)
        eventosRecyclerView.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }
    }
}