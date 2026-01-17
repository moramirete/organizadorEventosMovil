package com.example.organizadoreventosmovil

import com.example.organizadoreventosmovil.Constructores.Evento

object EventoRepository {
    // Esta es la lista que actúa como nuestra base de datos en memoria
    private val eventos = mutableListOf(
        Evento(id = 1, nombre = "Boda de Ana y Juan", fecha = "25/12/2024", lugar = "Salón Imperial", distribucion = emptyList()),
        Evento(id = 2, nombre = "Cumpleaños de Carlos", fecha = "15/01/2025", lugar = "Casa del Lago", distribucion = emptyList()),
        Evento(id = 3, nombre = "Conferencia de Tecnología", fecha = "05/03/2025", lugar = "Centro de Convenciones", distribucion = emptyList())
    )
    private var nextId = 4L // Un contador para generar IDs únicos

    fun getEventos(): List<Evento> {
        // Devolvemos una copia de la lista para evitar modificaciones accidentales
        return eventos.toList()
    }

    fun addEvento(evento: Evento) {
        // Creamos una copia del evento, le asignamos un ID nuevo y lo guardamos
        val nuevoEvento = evento.copy(id = nextId++)
        eventos.add(0, nuevoEvento) // Lo añadimos al principio para que aparezca primero
    }

    fun eliminarEvento(eventoId: Long) {
        eventos.removeAll { it.id == eventoId }
    }
}
