package com.example.organizadoreventosmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Importación necesaria para los botones
import androidx.appcompat.app.AppCompatActivity

class VisualizarEvento1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_evento1)

        // 1. Inicializar botones (DEBEN COINCIDIR CON LOS IDS DEL XML)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // 2. Lógica para CONTINUAR a la segunda vista
        btnContinue.setOnClickListener {
            val intent = Intent(this, VisualizarEvento2Activity::class.java)
            startActivity(intent)
        }

        // 3. Lógica para VOLVER ATRÁS (cierra esta Activity y vuelve a HomeActivity)
        btnBack.setOnClickListener {
            finish()
        }
    }
}