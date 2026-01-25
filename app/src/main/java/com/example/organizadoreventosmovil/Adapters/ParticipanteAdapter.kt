package com.example.organizadoreventosmovil.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Constructores.Participante
import com.example.organizadoreventosmovil.R

class ParticipanteAdapter(
    private val participantes: MutableList<Participante>,
    private val onEliminarClick: (Participante) -> Unit,
    private val onEditarClick: ((Participante) -> Unit)? = null
) : RecyclerView.Adapter<ParticipanteAdapter.ParticipanteViewHolder>() {

    class ParticipanteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPreferencias: TextView = view.findViewById(R.id.tvPreferencias)
        val btnEditar: ImageButton? = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipanteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participante, parent, false)
        return ParticipanteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipanteViewHolder, position: Int) {
        val participante = participantes[position]
        holder.tvNombre.text = participante.nombre
        
        val prefiere = if (participante.prefiere.isNotEmpty()) "Prefiere: ${participante.prefiere}" else ""
        val noPrefiere = if (participante.noPrefiere.isNotEmpty()) "No prefiere: ${participante.noPrefiere}" else ""
        
        holder.tvPreferencias.text = listOf(prefiere, noPrefiere).filter { it.isNotEmpty() }.joinToString(" | ")

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(participante)
        }
        
        holder.btnEditar?.apply {
            if (onEditarClick != null) {
                visibility = View.VISIBLE
                setOnClickListener { onEditarClick.invoke(participante) }
            } else {
                visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = participantes.size
}
