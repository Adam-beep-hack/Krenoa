package com.nexora.app.data

import android.content.Context

fun sauvegarderIdees(context: Context, idees: List<String>) {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("idees", idees.joinToString(";;")).apply()
}

fun chargerIdees(context: Context): List<String> {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texte = prefs.getString("idees", "") ?: ""
    if (texte.isBlank()) return emptyList()
    return texte.split(";;").filter { it.isNotBlank() }
}
