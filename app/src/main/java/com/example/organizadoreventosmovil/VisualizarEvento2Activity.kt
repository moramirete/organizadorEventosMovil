package com.example.organizadoreventosmovil

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Adapters.MesaAdapter
import com.example.organizadoreventosmovil.Constructores.Mesa

class VisualizarEvento2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_evento2)

        val mesasRecyclerView: RecyclerView = findViewById(R.id.mesasRecyclerView)

        val mesas = listOf(
            Mesa(
                "Mesa 1",
                8,
                listOf(
                    "Juan Pérez",
                    "Ana García",
                    "Luis Rodríguez",
                    "María Fernández",
                    "Carlos Sánchez",
                    "Laura Gómez",
                    "Miguel Martínez",
                    "Sofía López"
                )
            ),
            Mesa(
                "Mesa 2",
                10,
                listOf(
                    "David Jiménez",
                    "Elena Castillo",
                    "Javier Ruiz",
                    "Isabel Navarro",
                    "Francisco Vargas",
                    "Raquel Serrano",
                    "Daniel Romero",
                    "Carmen Ortega",
                    "Pablo Medina",
                    "Natalia Prieto"
                )
            ),
            Mesa(
                "Mesa 3",
                6,
                listOf(
                    "Andrés Molina",
                    "Beatriz Gil",
                    "Óscar Crespo",
                    "Teresa Ramos",
                    "Rubén Soto",
                    "Lorena Pascual"
                )
            ),
            Mesa(
                "Mesa 4",
                8,
                listOf(
                    "Fernando Alonso",
                    "Silvia Reyes",
                    "Ricardo Sanz",
                    "Mónica Santos",
                    "Adrián Soler",
                    "Verónica Vidal",
                    "César Bravo",
                    "Esther Blasco"
                )
            ),
            Mesa(
                "Mesa 5",
                10,
                listOf(
                    "Jorge Sáez",
                    "Cristina Vega",
                    "Manuel Rivas",
                    "Nerea Campos",
                    "Álvaro Ibáñez",
                    "Fátima Marín",
                    "Guillermo Núñez",
                    "Rocío Peña",
                    "Héctor Parra",
                    "Eva Durán"
                )
            ),
            Mesa(
                "Mesa 6",
                6,
                listOf(
                    "Ignacio arias",
                    "Lidia Fuentes",
                    "Mario Cano",
                    "Noelia Aguilar",
                    "Samuel arias",
                    "Pilar arias"
                )
            ),
            Mesa(
                "Mesa 7",
                8,
                listOf(
                    "Roberto arias",
                    "Marina arias",
                    "Sergio arias",
                    "Rosa intros",
                    "Diego arias",
                    "Inés arias",
                    "Iván arias",
                    "Nuria arias"
                )
            ),
            Mesa(
                "Mesa 8",
                10,
                listOf(
                    "Víctor arias",
                    "Raquel arias",
                    "Alberto arias",
                    "Clara arias",
                    "Félix arias",
                    "Lara arias",
                    "Marcos arias",
                    "Paula arias",
                    "Santiago arias",
                    "Aitana arias"
                )
            )
        )

        val adapter = MesaAdapter(mesas) { mesa ->
            val participantes = mesa.participantes.joinToString("\n")
            AlertDialog.Builder(this)
                .setTitle("Participantes de ${mesa.nombre}")
                .setMessage(participantes)
                .setPositiveButton("Aceptar", null)
                .show()
        }

        mesasRecyclerView.layoutManager = LinearLayoutManager(this)
        mesasRecyclerView.adapter = adapter

        val btnBack: Button = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()

        }
    }
}