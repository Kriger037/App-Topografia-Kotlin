package com.felipe.topografiaapp.presentation.import_coords

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.felipe.topografiaapp.R

class ArchivoAdapter(
    private val archivos: List<String>,
    private val onArchivoClick: (String) -> Unit
) : RecyclerView.Adapter<ArchivoAdapter.ArchivoViewHolder>() {

    class ArchivoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreArchivo: TextView = itemView.findViewById(R.id.tvNombreArchivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_archivo, parent, false)
        return ArchivoViewHolder(view)
    }

    override fun getItemCount() = archivos.size

    override fun onBindViewHolder(holder: ArchivoViewHolder, position: Int) {
        val archivo = archivos[position]
        holder.tvNombreArchivo.text = archivo
        holder.itemView.setOnClickListener {
            onArchivoClick(archivo)
        }
    }
}