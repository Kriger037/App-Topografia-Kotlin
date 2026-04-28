package com.felipe.topografiaapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalDataManager(context: Context) {

    private val prefs = context.getSharedPreferences("topografia_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Guardamos los fundos
    fun guardarFundos(lista: List<Fundo>) {
        val jsonText = gson.toJson(lista)
        prefs.edit().putString("lista_fundos_maestra", jsonText).apply()
    }

    fun leerFundos(): List<Fundo> {
        val jsonText = prefs.getString("lista_fundos_maestra", null) ?: return emptyList()
        val tipo = object : TypeToken<List<Fundo>>() {}.type
        return gson.fromJson(jsonText, tipo)
    }


    // Guardamos las canchas asociadas a un ID de Fundo específico
    fun guardarCanchasPorFundo(codigoFundo: String, lista: List<Cancha>) {
        val jsonText = gson.toJson(lista)
        prefs.edit().putString("canchas_del_fundo_$codigoFundo", jsonText).apply()
    }

    fun leerCanchasPorFundo(codigoFundo: String): List<Cancha> {
        val jsonText = prefs.getString("canchas_del_fundo_$codigoFundo", null) ?: return emptyList()
        val tipo = object : TypeToken<List<Cancha>>() {}.type
        return gson.fromJson(jsonText, tipo)
    }


    // Guardamos los PRs asociados a un ID de Cancha específico
    fun guardarPRsPorCancha(idCancha: Int, lista: List<PR>) {
        val jsonText = gson.toJson(lista)
        prefs.edit().putString("prs_de_cancha_$idCancha", jsonText).apply()
    }

    fun leerPRsPorCancha(idCancha: Int): List<PR> {
        val jsonText = prefs.getString("prs_de_cancha_$idCancha", null) ?: return emptyList()
        val tipo = object : TypeToken<List<PR>>() {}.type
        return gson.fromJson(jsonText, tipo)
    }
}