package com.felipe.topografiaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.felipe.topografiaapp.domain.model.Fundo

class FundoAdapter(
    private var listaCompleta: List<Fundo>,
    private val esAdmin: Boolean = false,
    private val onEliminarClick: ((Fundo) -> Unit)? = null
) : RecyclerView.Adapter<FundoAdapter.FundoViewHolder>() {

    private var listaFiltrada: List<Fundo> = listaCompleta.toList()

    class FundoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreFundo: TextView = itemView.findViewById(R.id.tvNombreFundo)
        val tvCodigoFundo: TextView = itemView.findViewById(R.id.tvCodigoFundo)
        val tvComuna: TextView = itemView.findViewById(R.id.tvComuna)
        val btnEliminar: View = itemView.findViewById(R.id.btnEliminarFundo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FundoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fundo, parent, false)
        return FundoViewHolder(view)
    }

    override fun getItemCount() = listaFiltrada.size

    override fun onBindViewHolder(holder: FundoViewHolder, position: Int) {
        val fundo = listaFiltrada[position]
        holder.tvNombreFundo.text = fundo.nombreFundo
        holder.tvCodigoFundo.text = "Código: ${fundo.codigoFundo}"
        holder.tvComuna.text = "Comuna: ${fundo.comuna ?: "Sin información"}"

        // Mostrar botón eliminar solo para Admin
        holder.btnEliminar.visibility = if (esAdmin) View.VISIBLE else View.GONE
        holder.btnEliminar.setOnClickListener {
            onEliminarClick?.invoke(fundo)
        }

        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(it.context, CanchasActivity::class.java)
            intent.putExtra("CODIGO_FUNDO", fundo.codigoFundo)
            intent.putExtra("NOMBRE_FUNDO", fundo.nombreFundo)
            it.context.startActivity(intent)
        }
    }

    // Filtrar por nombre o código
    fun filtrar(texto: String) {
        listaFiltrada = if (texto.isEmpty()) {
            listaCompleta.toList()
        } else {
            listaCompleta.filter {
                it.nombreFundo.contains(texto, ignoreCase = true) ||
                        it.codigoFundo.contains(texto, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}