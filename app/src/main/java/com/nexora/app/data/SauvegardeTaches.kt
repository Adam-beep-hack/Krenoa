package com.nexora.app.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.nexora.app.RappelReceiver
import com.nexora.app.model.Frequence
import com.nexora.app.model.Priorite
import com.nexora.app.model.Tache
import java.util.Calendar

fun programmerAlarme(context: Context, tache: Tache) {
    if (tache.rappelMillis <= 0L) return
    val gestionnaireAlarme = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    var millisCible = tache.rappelMillis

    if (tache.frequence == Frequence.PERSONNALISE && tache.joursRepetition.isNotEmpty()) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = tache.rappelMillis
        if (!tache.joursRepetition.contains(cal.get(Calendar.DAY_OF_WEEK))) {
            for (i in 1..7) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                if (tache.joursRepetition.contains(cal.get(Calendar.DAY_OF_WEEK))) break
            }
            millisCible = cal.timeInMillis
        }
    }

    val intention = Intent(context, RappelReceiver::class.java).apply {
        putExtra("titre", tache.titre)
        putExtra("frequence", tache.frequence.name)
        putExtra("jours", tache.joursRepetition.joinToString(","))
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        tache.titre.hashCode(),
        intention,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    gestionnaireAlarme.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        millisCible,
        pendingIntent
    )
}

fun sauvegarderTaches(context: Context, taches: List<Tache>) {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texteASauvegarder = taches.joinToString(";;") {
        "${it.titre}~${if (it.terminee) 1 else 0}~${it.rappel}~${it.rappelMillis}~${it.priorite.name}~${it.frequence.name}~${it.joursRepetition.joinToString(",")}"
    }
    prefs.edit().putString("taches", texteASauvegarder).apply()
}

fun chargerTaches(context: Context): List<Tache> {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texte = prefs.getString("taches", "") ?: ""
    if (texte.isBlank()) return emptyList()
    return texte.split(";;").mapNotNull { ligne ->
        val parties = ligne.split("~")
        val priorite = if (parties.size >= 5) {
            try { Priorite.valueOf(parties[4]) } catch (e: Exception) { Priorite.MOYENNE }
        } else Priorite.MOYENNE
        val frequence = if (parties.size >= 6) {
            try { Frequence.valueOf(parties[5]) } catch (e: Exception) { Frequence.UNIQUE }
        } else Frequence.UNIQUE
        val jours = if (parties.size >= 7 && parties[6].isNotBlank()) {
            parties[6].split(",").mapNotNull { it.toIntOrNull() }.toSet()
        } else emptySet()
        when {
            parties.size >= 4 -> Tache(parties[0], parties[1] == "1", parties[2], parties[3].toLongOrNull() ?: 0L, priorite, frequence, jours)
            parties.size == 3 -> Tache(parties[0], parties[1] == "1", parties[2])
            parties.size == 2 -> Tache(parties[0], parties[1] == "1")
            else -> null
        }
    }
}
