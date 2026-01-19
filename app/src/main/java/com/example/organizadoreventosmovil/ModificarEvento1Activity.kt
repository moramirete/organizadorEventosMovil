package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.EventoModificarAdapter
import com.example.organizadoreventosmovil.Constructores.Evento
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ModificarEvento1Activity : AppCompatActivity() {

    private lateinit var adapter: EventoModificarAdapter
    private lateinit var eventosRecyclerView: RecyclerView
    private var eventos = mutableListOf<Evento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_evento1)

        eventosRecyclerView = findViewById(R.id.eventosRecyclerView)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        // Configuración del Adaptador con lógica de Nube
        adapter = EventoModificarAdapter(
            eventos,
            onEditClick = { evento ->
                val intent = Intent(this, NuevoEvento1Activity::class.java)
                intent.putExtra("EVENTO_ID", evento.id)
                intent.putExtra("IS_EDIT_MODE", true)
                startActivity(intent)
            },
            onDeleteClick = { evento ->
                eliminarEventoDeNube(evento)
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
        cargarEventosDesdeNube()
    }

    private fun cargarEventosDesdeNube() {
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user == null) return

        lifecycleScope.launch {
            try {
                // Consultamos solo los eventos que pertenecen al usuario logueado
                val lista = SupabaseClient.client.postgrest["eventos"]
                    .select {
                        filter {
                            eq("usuario_id", user.id)
                        }
                    }
                    .decodeList<Evento>()

                eventos.clear()
                eventos.addAll(lista)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error al cargar: ${e.message}")
                Toast.makeText(this@ModificarEvento1Activity, "Error al sincronizar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarEventoDeNube(evento: Evento) {
        val idEvento = evento.id ?: return

        lifecycleScope.launch {
            try {
                // Eliminamos de la tabla "eventos" donde coincida el ID
                SupabaseClient.client.postgrest["eventos"].delete {
                    filter {
                        eq("id", idEvento)
                    }
                }

                Toast.makeText(this@ModificarEvento1Activity, "Evento '${evento.nombre}' eliminado", Toast.LENGTH_SHORT).show()

                // Refrescamos la lista localmente para no tener que volver a descargar todo
                val posicion = eventos.indexOf(evento)
                if (posicion != -1) {
                    eventos.removeAt(posicion)
                    adapter.notifyItemRemoved(posicion)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error al eliminar: ${e.message}")
                Toast.makeText(this@ModificarEvento1Activity, "No se pudo eliminar el evento", Toast.LENGTH_SHORT).show()
            }
        }
    }
}