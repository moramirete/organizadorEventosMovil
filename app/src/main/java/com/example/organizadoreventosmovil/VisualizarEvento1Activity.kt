package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VisualizarEvento1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_evento1)

        val eventosRecyclerView: RecyclerView = findViewById(R.id.eventosRecyclerView)

        val eventos = listOf(
            Evento("Boda de Ana y Juan", "25 de diciembre, 2024"),
            Evento("Cumpleaños de María", "15 de enero, 2025"),
            Evento("Aniversario de Bodas", "02 de febrero, 2025"),
            Evento("Fiesta de Fin de Año", "31 de diciembre, 2024"),
            Evento("Conferencia de Tecnología", "10 de marzo, 2025"),
            Evento("Bautizo de Sofía", "20 de abril, 2025"),
            Evento("Reunión familiar", "05 de mayo, 2025"),
            Evento("Despedida de Soltero", "18 de mayo, 2025"),
            Evento("Baby Shower", "30 de junio, 2025")
        )

        val adapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, VisualizarEvento2Activity::class.java)
            // Opcional: pasar datos del evento a la siguiente actividad
            intent.putExtra("nombre_evento", evento.nombre)
            startActivity(intent)
        }

        eventosRecyclerView.layoutManager = LinearLayoutManager(this)
        eventosRecyclerView.adapter = adapter

        val btnBack: Button = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

}
