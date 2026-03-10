package com.felipe.topografiaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CanchaAdapter(private val listaCanchas: List<Cancha>) :
    RecyclerView.Adapter<CanchaAdapter.CanchaViewHolder>() {

    class CanchaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumeroCancha: TextView = itemView.findViewById(R.id.tvNumeroCancha)
        val tvFechaActualizacion: TextView = itemView.findViewById(R.id.tvFechaActualizacion)
        val ivDescargarTxt: ImageView = itemView.findViewById(R.id.ivDescargarTxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanchaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cancha, parent, false)
        return CanchaViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaCanchas.size
    }

    override fun onBindViewHolder(holder: CanchaViewHolder, position: Int) {
        val canchaActual = listaCanchas[position]

        holder.tvNumeroCancha.text = canchaActual.numero_cancha
        holder.tvFechaActualizacion.text = "Última mod: ${canchaActual.fecha_actualizacion}"

        holder.itemView.setOnClickListener { view ->
            Toast.makeText(view.context, "Próximamente: Ver PRs de ${canchaActual.numero_cancha}", Toast.LENGTH_SHORT).show()
        }

        holder.ivDescargarTxt.setOnClickListener { view ->
            Toast.makeText(view.context, "Función futura: Descargando TXT de ${canchaActual.numero_cancha}...", Toast.LENGTH_SHORT).show()
        }
    }
}