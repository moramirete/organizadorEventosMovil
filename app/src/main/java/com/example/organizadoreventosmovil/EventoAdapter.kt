package com.example.organizadoreventosmovil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventoAdapter(
    private val eventos: List<Evento>,
    private val onItemClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.bind(evento)
        holder.itemView.setOnClickListener { onItemClick(evento) }
    }

    override fun getItemCount(): Int = eventos.size

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.eventoNombre)
        private val fechaTextView: TextView = itemView.findViewById(R.id.eventoFecha)
        private val iconImageView: ImageView = itemView.findViewById(R.id.eventoIcon)

        fun bind(evento: Evento) {
            nombreTextView.text = evento.nombre
            fechaTextView.text = evento.fecha
            // Aquí puedes personalizar el icono si lo deseas
        }
    }
}
