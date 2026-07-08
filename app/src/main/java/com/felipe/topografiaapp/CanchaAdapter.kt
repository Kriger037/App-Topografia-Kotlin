package com.felipe.topografiaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.felipe.topografiaapp.data.local.entity.CanchaEntity
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.felipe.topografiaapp.PR
import com.felipe.topografiaapp.data.local.entity.PREntity

class CanchaAdapter(private val listaCanchas: List<CanchaEntity>) :
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

        holder.ivDescargarTxt.setOnClickListener { view ->
            val contexto = view.context
            Toast.makeText(contexto, "Descargando puntos para terreno...", Toast.LENGTH_SHORT).show()

            val localDataManager = LocalDataManager(contexto)

            RetrofitClient.api.obtenerPRs(canchaActual.id)
                .enqueue(object : Callback<List<PR>> {
                    override fun onResponse(call: Call<List<PR>>, response: Response<List<PR>>) {
                        if (response.isSuccessful) {
                            val listaPRs = response.body()
                            if (listaPRs != null && listaPRs.isNotEmpty()) {

                                // Convertir de PR (modelo viejo) a PREntity (modelo nuevo)
                                val listaEntities = listaPRs.map { pr ->
                                    PREntity(
                                        id = pr.id,
                                        canchaId = canchaActual.id,
                                        descriptor = pr.descriptor,
                                        norte = pr.norte,
                                        este = pr.este,
                                        cota = pr.cota,
                                        latitud = pr.latitud,
                                        longitud = pr.longitud,
                                        fechaCreacion = pr.fecha_creacion,
                                        fechaModificacion = pr.fecha_modificacion
                                    )
                                }

                                (contexto as? LifecycleOwner)?.lifecycleScope?.launch {
                                    localDataManager.guardarPRsPorCancha(canchaActual.id, listaEntities)
                                    Toast.makeText(contexto, "¡Listo! Puntos de ${canchaActual.numeroCancha} guardados.", Toast.LENGTH_LONG).show()
                                }

                            } else {
                                Toast.makeText(contexto, "Esta cancha no tiene puntos topográficos.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(contexto, "Error del servidor al descargar.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<PR>>, t: Throwable) {
                        Toast.makeText(contexto, "Falló la red al intentar descargar.", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}