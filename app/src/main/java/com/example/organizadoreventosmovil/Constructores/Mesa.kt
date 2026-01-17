package com.example.organizadoreventosmovil.Constructores

import kotlinx.serialization.Serializable

@Serializable
data class Mesa(
    val numero: Int,
    var capacidad: Int = 10, // Capacidad por defecto
    var participantes: MutableList<Participante> = mutableListOf()
)
