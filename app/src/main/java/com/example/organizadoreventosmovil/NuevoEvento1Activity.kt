package com.example.organizadoreventosmovil

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NuevoEvento1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento1)

        val btnVolver = findViewById<Button>(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish()
        }
    }
}