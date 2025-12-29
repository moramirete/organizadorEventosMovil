package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.EventoAdapter

class VisualizarEvento1Activity : AppCompatActivity() {
    
    private lateinit var adapter: EventoAdapter
    private lateinit var eventosRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_evento1)

        eventosRecyclerView = findViewById(R.id.eventosRecyclerView)
        val btnBack: Button = findViewById(R.id.btnBack)

        // Usamos el repositorio compartido
        val eventos = EventoRepository.getEventos()

        adapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, VisualizarEvento2Activity::class.java)
            // Aquí puedes pasar datos extra si es necesario
            startActivity(intent)
        }

        eventosRecyclerView.layoutManager = LinearLayoutManager(this)
        eventosRecyclerView.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refrescamos la lista al volver a esta pantalla por si hubo cambios
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }
}
