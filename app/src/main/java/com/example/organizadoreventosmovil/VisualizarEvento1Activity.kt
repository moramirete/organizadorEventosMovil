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
import com.example.organizadoreventosmovil.Adapters.EventoAdapter
import com.example.organizadoreventosmovil.Constructores.Evento
// CORRECCIÓN DE IMPORTS:
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
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
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        cargarEventosDesdeNube()
    }

    private fun setupRecyclerView() {
        eventoAdapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, VisualizarEvento2Activity::class.java)
            val eventoJson = Json.encodeToString(evento)
            intent.putExtra("EVENTO_JSON", eventoJson)
            startActivity(intent)
        }
        eventosRecyclerView.layoutManager = LinearLayoutManager(this)
        eventosRecyclerView.adapter = eventoAdapter
    }

    private fun cargarEventosDesdeNube() {
        // Acceso al cliente usando los imports corregidos
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user == null) return

        LoadingUtils.showLoading(this) // Mostrar loading
        lifecycleScope.launch {
            try {
                val lista = SupabaseClient.client.postgrest["eventos"]
                    .select {
                        filter {
                            eq("usuario_id", user.id)
                        }
                    }
                    .decodeList<Evento>()

                eventos.clear()
                eventos.addAll(lista)
                eventoAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("SUPABASE", "Error cargando: ${e.message}")
                Toast.makeText(this@VisualizarEvento1Activity, "Error al sincronizar", Toast.LENGTH_SHORT).show()
            } finally {
                LoadingUtils.hideLoading() // Ocultar loading
            }
        }
    }
}