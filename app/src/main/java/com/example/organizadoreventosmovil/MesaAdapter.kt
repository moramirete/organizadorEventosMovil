package com.example.organizadoreventosmovil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MesaAdapter(
    private val mesas: List<Mesa>,
    private val onItemClick: (Mesa) -> Unit
) : RecyclerView.Adapter<MesaAdapter.MesaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mesa, parent, false)
        return MesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MesaViewHolder, position: Int) {
        val mesa = mesas[position]
        holder.bind(mesa)
        holder.itemView.setOnClickListener { onItemClick(mesa) }
    }

    override fun getItemCount(): Int = mesas.size

    class MesaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.mesaNombre)
        private val personasTextView: TextView = itemView.findViewById(R.id.mesaPersonas)
        private val iconImageView: ImageView = itemView.findViewById(R.id.mesaIcon)

        fun bind(mesa: Mesa) {
            nombreTextView.text = mesa.nombre
            personasTextView.text = "${mesa.personas} Personas"
            // Aquí puedes personalizar el icono si lo deseas
        }
    }
}
