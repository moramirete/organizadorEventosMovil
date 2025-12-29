package com.example.organizadoreventosmovil.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadoreventosmovil.Constructores.Mesa
import com.example.organizadoreventosmovil.R

class MesaAdapter(
    private val mesas: List<Mesa>,
    private val onMesaClick: (Mesa) -> Unit
) : RecyclerView.Adapter<MesaAdapter.MesaViewHolder>() {

    class MesaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumero: TextView = view.findViewById(R.id.tvNumeroMesa)
        val tvParticipantes: TextView = view.findViewById(R.id.tvParticipantesMesa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mesa, parent, false)
        return MesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MesaViewHolder, position: Int) {
        val mesa = mesas[position]
        holder.tvNumero.text = "Mesa ${mesa.numero}"
        
        if (mesa.participantes.isEmpty()) {
            holder.tvParticipantes.text = "Vacía"
        } else {
            val nombres = mesa.participantes.joinToString("\n") { it.nombre }
            holder.tvParticipantes.text = nombres
        }

        holder.itemView.setOnClickListener {
            onMesaClick(mesa)
        }
    }

    override fun getItemCount() = mesas.size
}
