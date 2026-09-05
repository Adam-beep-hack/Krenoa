package com.nexora.app.data

import com.nexora.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun demanderAGemini(question: String): String = withContext(Dispatchers.IO) {
    val nombreMaxEssais = 3
    var dernierMessage = "Erreur inconnue"

    for (essai in 1..nombreMaxEssais) {
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
            val connexion = url.openConnection() as HttpURLConnection
            connexion.requestMethod = "POST"
            connexion.setRequestProperty("Content-Type", "application/json")
            connexion.doOutput = true

            val corps = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(
                        JSONObject().put("text", question)
                    ))
                ))
            }

            connexion.outputStream.use { it.write(corps.toString().toByteArray()) }

            val code = connexion.responseCode
            if (code == 200) {
                val reponse = connexion.inputStream.bufferedReader().readText()
                val json = JSONObject(reponse)
                return@withContext json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } else if (code == 503 || code == 429) {
                // Modèle surchargé ou trop de requêtes : on attend puis on réessaie
                dernierMessage = "Le modèle est momentanément surchargé (code $code)."
                delay(1500L * essai) // attend un peu plus longtemps à chaque essai
            } else {
                val erreur = connexion.errorStream?.bufferedReader()?.readText() ?: "Erreur inconnue"
                return@withContext "Erreur ($code) : $erreur"
            }
        } catch (e: Exception) {
            dernierMessage = "Erreur de connexion : ${e.message}"
            delay(1000L)
        }
    }

    "Désolé, l'assistant est indisponible pour le moment. $dernierMessage Réessaie dans quelques instants."
}
