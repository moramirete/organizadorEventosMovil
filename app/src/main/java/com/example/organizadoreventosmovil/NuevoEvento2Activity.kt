package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.ParticipanteAdapter
import com.example.organizadoreventosmovil.Constructores.Participante
import com.google.android.material.textfield.TextInputEditText

class NuevoEvento2Activity : AppCompatActivity() {

    private val participantes = mutableListOf<Participante>()
    private lateinit var adapter: ParticipanteAdapter
    private var numeroMesas = 5 // Valor por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento2)

        // Recibir número de mesas de la actividad anterior
        numeroMesas = intent.getIntExtra("NUMERO_MESAS", 5)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreParticipante)
        val etPrefiere = findViewById<TextInputEditText>(R.id.etPrefiere)
        val etNoPrefiere = findViewById<TextInputEditText>(R.id.etNoPrefiere)
        val btnAgregar = findViewById<Button>(R.id.btnAgregarParticipante)
        val rvParticipantes = findViewById<RecyclerView>(R.id.rvParticipantes)
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguiente)

        // Configurar RecyclerView
        adapter = ParticipanteAdapter(participantes) { participante ->
            participantes.remove(participante)
            adapter.notifyDataSetChanged()
        }
        rvParticipantes.layoutManager = LinearLayoutManager(this)
        rvParticipantes.adapter = adapter

        // Agregar Participante
        btnAgregar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val prefiere = etPrefiere.text.toString()
            val noPrefiere = etNoPrefiere.text.toString()

            if (nombre.isNotEmpty()) {
                val nuevoParticipante = Participante(nombre, prefiere, noPrefiere)
                participantes.add(nuevoParticipante)
                adapter.notifyDataSetChanged()
                
                // Limpiar campos
                etNombre.text?.clear()
                etPrefiere.text?.clear()
                etNoPrefiere.text?.clear()
                etNombre.requestFocus()
            } else {
                Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            }
        }

        // Botones Inferiores
        btnVolver.setOnClickListener {
            finish()
        }

        btnSiguiente.setOnClickListener {
            val intent = Intent(this, AsignacionParticipantesActivity::class.java)
            // Pasar lista de participantes a la siguiente actividad
            intent.putParcelableArrayListExtra("LISTA_PARTICIPANTES", ArrayList(participantes))
            // Pasar el número de mesas a la siguiente actividad
            intent.putExtra("NUMERO_MESAS", numeroMesas)
            startActivity(intent)
        }
    }
}
