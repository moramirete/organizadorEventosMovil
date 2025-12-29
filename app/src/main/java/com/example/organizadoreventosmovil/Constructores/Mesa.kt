package com.example.organizadoreventosmovil.Constructores

data class Mesa(
    val numero: Int,
    val capacidad: Int = 10, // Capacidad por defecto, puede ser variable
    val participantes: MutableList<Participante> = mutableListOf()
)
