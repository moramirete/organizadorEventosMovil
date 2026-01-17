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
    // Mantenemos la lista como un campo de la clase para poder actualizarla
    private var eventos = mutableListOf<Evento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_evento1)

        eventosRecyclerView = findViewById(R.id.eventosRecyclerView)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        // Pasamos la lista mutable al adaptador. Se llenará en onResume.
        adapter = EventoModificarAdapter(
            eventos,
            onEditClick = { evento ->
                val intent = Intent(this, NuevoEvento1Activity::class.java)
                // Pasamos el ID para que la pantalla de edición sepa qué evento modificar
                intent.putExtra("EVENTO_ID", evento.id)
                intent.putExtra("IS_EDIT_MODE", true)
                startActivity(intent)
            },
            onDeleteClick = { evento ->
                // 1. Eliminar del repositorio usando el ID
                evento.id?.let { id ->
                    EventoRepository.eliminarEvento(id)
                    Toast.makeText(this, "Evento '${evento.nombre}' eliminado", Toast.LENGTH_SHORT).show()
                }

                // 2. Refrescar la lista en la pantalla para que el cambio sea visible
                refreshEventList()
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
        // Refrescamos la lista cada vez que la pantalla se vuelve visible
        // para asegurar que los datos siempre estén actualizados.
        refreshEventList()
    }

    private fun refreshEventList() {
        // 1. Obtenemos la lista actualizada del repositorio
        val updatedEventos = EventoRepository.getEventos()

        // 2. Limpiamos la lista actual y añadimos los nuevos datos
        eventos.clear()
        eventos.addAll(updatedEventos)

        // 3. Notificamos al adaptador que el conjunto de datos ha cambiado
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }
}
