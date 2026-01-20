package com.example.organizadoreventosmovil.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Constructores.Evento
import com.example.organizadoreventosmovil.R

class EventoModificarAdapter(
    private val eventos: MutableList<Evento>,
    private val onEditClick: (Evento) -> Unit,
    private val onDeleteClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoModificarAdapter.EventoViewHolder>() {

    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.nombreEventoTextView)
        val fecha: TextView = view.findViewById(R.id.fechaEventoTextView)
        val lugar: TextView = view.findViewById(R.id.lugarEventoTextView)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento_modificar, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.nombre.text = evento.nombre
        holder.fecha.text = "Fecha: ${evento.fecha ?: ""}"
        holder.lugar.text = "Ubicación: ${evento.ubicacion ?: ""}"

        holder.btnEditar.setOnClickListener { onEditClick(evento) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(evento) }
    }

    override fun getItemCount() = eventos.size
}