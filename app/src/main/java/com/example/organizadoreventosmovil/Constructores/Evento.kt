package com.example.organizadoreventosmovil.Constructores

import kotlinx.serialization.Serializable

@Serializable
data class Evento(
    val id: Long? = null, // ID único de la base de datos (autogenerado)
    val nombre: String,
    val fecha: String,
    val lugar: String,
    val distribucion: List<Mesa> = emptyList()
)
