package com.felipe.topografiaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.animation.Positioning


class PRAdapter(private val listaPRs: List<PR>): RecyclerView.Adapter<PRAdapter.PRViewHolder>() {
    class PRViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDescriptor: TextView = itemView.findViewById(R.id.tvDescriptor)
        val tvNorte: TextView = itemView.findViewById(R.id.tvNorte)
        val tvEste: TextView = itemView.findViewById(R.id.tvEste)
        val tvCota: TextView = itemView.findViewById(R.id.tvCota)
        val tvFechaModPR: TextView = itemView.findViewById(R.id.tvFechaModPR)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PRViewHolder{
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pr, parent, false)
        return PRViewHolder(view)
    }

    override fun getItemCount(): Int{
        return listaPRs.size
    }

    override fun onBindViewHolder(holder: PRViewHolder, position: Int){
        val prActual = listaPRs[position]

        holder.tvDescriptor.text = prActual.descriptor
        holder.tvNorte.text = String.format("%.3f", prActual.norte)
        holder.tvEste.text = String.format("%.3f", prActual.este)
        holder.tvCota.text = String.format("%.3f", prActual.cota)

        val soloFecha = prActual.fecha_modificacion.substringBefore(" ")
        holder.tvFechaModPR.text = "Última mod: $soloFecha"
    }
}