package com.example.organizadoreventosmovil.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Constructores.Evento
import com.example.organizadoreventosmovil.R

class EventoAdapter(private val eventos: List<Evento>, private val onItemClick: (Evento) -> Unit) :
    RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.nombreEvento.text = evento.nombre
        holder.fechaEvento.text = "Fecha: ${evento.fecha ?: ""}"
        // Usamos .ubicacion porque es el nombre en tu clase Evento
        holder.lugarEvento.text = "Lugar: ${evento.ubicacion ?: ""}"

        holder.itemView.setOnClickListener {
            onItemClick(evento)
        }
    }

    override fun getItemCount(): Int = eventos.size

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // IDs corregidos según tu item_evento.xml
        val nombreEvento: TextView = itemView.findViewById(R.id.nombreEventoTextView)
        val fechaEvento: TextView = itemView.findViewById(R.id.fechaEventoTextView)
        val lugarEvento: TextView = itemView.findViewById(R.id.lugarEventoTextView)
    }
}