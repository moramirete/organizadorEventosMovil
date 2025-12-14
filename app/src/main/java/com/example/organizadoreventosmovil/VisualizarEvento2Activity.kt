package com.example.organizadoreventosmovil

import android.os.Bundle
import android.widget.Button // Importación necesaria para el botón
import androidx.appcompat.app.AppCompatActivity

class VisualizarEvento2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_evento2)

        // 1. Inicializar botón de volver atrás
        val btnBack = findViewById<Button>(R.id.btnBack)

        // 2. Lógica para VOLVER ATRÁS (cierra esta Activity y vuelve a VisualizarEvento1Activity)
        btnBack.setOnClickListener {
            finish()
        }
    }
}