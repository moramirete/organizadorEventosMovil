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
    
    // Variables para almacenar los datos del evento
    private var nombreEvento: String? = null
    private var fechaEvento: String? = null
    private var lugarEvento: String? = null
    private var numeroMesas = 5 // Valor por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento2)

        // Recibir los datos del evento de la actividad anterior
        nombreEvento = intent.getStringExtra("NOMBRE_EVENTO")
        fechaEvento = intent.getStringExtra("FECHA_EVENTO")
        lugarEvento = intent.getStringExtra("LUGAR_EVENTO")
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

            if (nombre.trim().isNotEmpty()) {
                val nuevoParticipante = Participante(nombre, prefiere, noPrefiere)
                participantes.add(nuevoParticipante)
                adapter.notifyDataSetChanged()
                
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
            
            // Pasar TODOS los datos a la siguiente actividad
            intent.putExtra("NOMBRE_EVENTO", nombreEvento)
            intent.putExtra("FECHA_EVENTO", fechaEvento)
            intent.putExtra("LUGAR_EVENTO", lugarEvento)
            intent.putExtra("NUMERO_MESAS", numeroMesas)
            intent.putParcelableArrayListExtra("LISTA_PARTICIPANTES", ArrayList(participantes))
            
            startActivity(intent)
        }
    }
}
