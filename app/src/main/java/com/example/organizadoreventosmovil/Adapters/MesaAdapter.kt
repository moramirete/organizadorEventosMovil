package com.example.organizadoreventosmovil.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Constructores.Mesa
import com.example.organizadoreventosmovil.R

class MesaAdapter(private val mesas: List<Mesa>, private val onItemClick: (Mesa) -> Unit) :
    RecyclerView.Adapter<MesaAdapter.MesaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mesa, parent, false)
        return MesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MesaViewHolder, position: Int) {
        val mesa = mesas[position]
        holder.nombreMesa.text = mesa.nombre
        holder.capacidadMesa.text = "Capacidad: ${mesa.capacidad}"
        holder.itemView.setOnClickListener {
            onItemClick(mesa)
        }
    }

    override fun getItemCount(): Int = mesas.size

    class MesaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreMesa: TextView = itemView.findViewById(R.id.nombreMesaTextView)
        val capacidadMesa: TextView = itemView.findViewById(R.id.capacidadMesaTextView)
    }
}