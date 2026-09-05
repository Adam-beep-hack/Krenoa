package com.nexora.app.data

import com.nexora.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun demanderAGemini(question: String): String = withContext(Dispatchers.IO) {
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
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } else {
            val erreur = connexion.errorStream?.bufferedReader()?.readText() ?: "Erreur inconnue"
            "Erreur ($code) : $erreur"
        }
    } catch (e: Exception) {
        "Erreur de connexion : ${e.message}"
    }
}
