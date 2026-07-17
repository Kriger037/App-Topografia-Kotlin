package com.felipe.topografiaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.felipe.topografiaapp.domain.model.Cancha

class CanchaAdapter(
    private val listaCanchas: List<Cancha>,
    private val onDescargarClick: (Cancha) -> Unit
) : RecyclerView.Adapter<CanchaAdapter.CanchaViewHolder>() {

    class CanchaViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val tvNumeroCancha: TextView = itemView.findViewById(R.id.tvNumeroCancha)
        val tvFechaActualizacion: TextView = itemView.findViewById(R.id.tvFechaActualizacion)
        val ivDescargarTxt: ImageView = itemView.findViewById(R.id.ivDescargarTxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanchaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cancha, parent, false)
        return CanchaViewHolder(view)
    }

    override fun getItemCount() = listaCanchas.size

    override fun onBindViewHolder(holder: CanchaViewHolder, position: Int) {
        val canchaActual = listaCanchas[position]

        holder.tvNumeroCancha.text = canchaActual.numeroCancha
        holder.tvFechaActualizacion.text = "Última mod: ${canchaActual.fechaActualizacion}"

        holder.itemView.setOnClickListener { view ->
            val intent = android.content.Intent(view.context, PRsActivity::class.java)
            intent.putExtra("CANCHA_ID", canchaActual.id)
            intent.putExtra("NUMERO_CANCHA", canchaActual.numeroCancha)

            val actividadActual = view.context as android.app.Activity
            intent.putExtra("CODIGO_FUNDO", actividadActual.intent.getStringExtra("CODIGO_FUNDO"))
            intent.putExtra("NOMBRE_FUNDO", actividadActual.intent.getStringExtra("NOMBRE_FUNDO"))

            view.context.startActivity(intent)
        }

        holder.ivDescargarTxt.setOnClickListener {
            onDescargarClick(canchaActual)
        }
    }
}