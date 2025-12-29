package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.EventoModificarAdapter
import com.example.organizadoreventosmovil.Constructores.Evento

class ModificarEvento1Activity : AppCompatActivity() {
    
    private lateinit var adapter: EventoModificarAdapter
    private lateinit var eventosRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_evento1)

        eventosRecyclerView = findViewById(R.id.eventosRecyclerView)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        // Usamos la lista del Repositorio Compartido
        val eventos = EventoRepository.getEventos()

        adapter = EventoModificarAdapter(
            eventos,
            onEditClick = { evento ->
                val intent = Intent(this, NuevoEvento1Activity::class.java)
                intent.putExtra("NOMBRE_EVENTO", evento.nombre)
                intent.putExtra("FECHA_EVENTO", evento.fecha)
                intent.putExtra("LUGAR_EVENTO", evento.lugar)
                intent.putExtra("IS_EDIT_MODE", true)
                startActivity(intent)
            },
            onDeleteClick = { evento ->
                // Eliminamos del repositorio
                EventoRepository.eliminarEvento(evento)
                
                Toast.makeText(this, "Evento eliminado: ${evento.nombre}", Toast.LENGTH_SHORT).show()
                
                // Notificamos al adaptador que los datos han cambiado
                adapter.notifyDataSetChanged()
            }
        )

        eventosRecyclerView.layoutManager = LinearLayoutManager(this)
        eventosRecyclerView.adapter = adapter

        btnVolver.setOnClickListener {
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refrescamos la lista al volver por si se agregaron/editaron eventos
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }
}
