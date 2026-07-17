package com.felipe.topografiaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.felipe.topografiaapp.domain.model.Fundo

// El Adaptador recibe como parametro la lista de fundos descargados de MySQL
class FundoAdapter(private val listaFundos: List<Fundo>) : RecyclerView.Adapter<FundoAdapter.FundoViewHolder>() {
    class FundoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreFundo: TextView = itemView.findViewById(R.id.tvNombreFundo)
        val tvCodigoFundo: TextView = itemView.findViewById(R.id.tvCodigoFundo)
        val tvComuna: TextView = itemView.findViewById(R.id.tvComuna)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FundoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_fundo, parent, false)
        return FundoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FundoViewHolder, position: Int){
        val fundoActual = listaFundos[position]

        holder.tvNombreFundo.text = fundoActual.nombreFundo
        holder.tvCodigoFundo.text = "Código: ${fundoActual.codigoFundo}"
        holder.tvComuna.text = "Comuna: ${fundoActual.comuna}"

        holder.itemView.setOnClickListener { view ->
            val intent = android.content.Intent(view.context, CanchasActivity::class.java)

            intent.putExtra("CODIGO_FUNDO", fundoActual.codigoFundo)
            intent.putExtra("NOMBRE_FUNDO", fundoActual.nombreFundo)

            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int{
        return listaFundos.size
    }
}