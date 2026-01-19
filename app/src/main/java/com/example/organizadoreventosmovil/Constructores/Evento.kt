package com.example.organizadoreventosmovil.Constructores

import kotlinx.serialization.Serializable

@Serializable
data class Evento(
    val id: String? = null,
    val usuario_id: String? = null,
    val nombre: String,
    val descripcion: String? = null,
    val fecha: String? = null,
    val ubicacion: String? = null,
    val telefono: String? = null,
    val num_participantes: Int? = null,
    val distribucion: List<Mesa> = emptyList()
)