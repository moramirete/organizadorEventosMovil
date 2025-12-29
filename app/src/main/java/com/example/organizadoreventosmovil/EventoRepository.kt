package com.example.organizadoreventosmovil

import com.example.organizadoreventosmovil.Constructores.Evento

object EventoRepository {
    // Esta es la lista compartida de eventos
    private val eventos = mutableListOf(
        Evento("Boda de Ana y Juan", "25/12/2024", "Salón Imperial"),
        Evento("Cumpleaños de Carlos", "15/01/2025", "Casa del Lago"),
        Evento("Conferencia de Tecnología", "05/03/2025", "Centro de Convenciones")
    )

    fun getEventos(): MutableList<Evento> {
        return eventos
    }

    fun eliminarEvento(evento: Evento) {
        eventos.remove(evento)
    }
}
