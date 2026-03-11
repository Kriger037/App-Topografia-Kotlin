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
            val intent = android.content.Intent(view.context, PRsActivity::class.java)

            // Datos que se enviaran a la activity de PRs
            intent.putExtra("CANCHA_ID", canchaActual.id)
            intent.putExtra("NUMERO_CANCHA", canchaActual.numero_cancha)

            // Datos heredados del fundo desde la pantalla anterior
            val actividadActual = view.context as android.app.Activity
            intent.putExtra("CODIGO_FUNDO", actividadActual.intent.getStringExtra("CODIGO_FUNDO"))
            intent.putExtra("NOMBRE_FUNDO", actividadActual.intent.getStringExtra("NOMBRE_FUNDO"))

            view.context.startActivity(intent)
        }

        holder.ivDescargarTxt.setOnClickListener { view ->
            Toast.makeText(view.context, "Función futura: Descargando TXT de ${canchaActual.numero_cancha}...", Toast.LENGTH_SHORT).show()
        }
    }
}