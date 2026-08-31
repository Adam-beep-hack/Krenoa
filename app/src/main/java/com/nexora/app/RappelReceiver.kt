package com.nexora.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class RappelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titre = intent.getStringExtra("titre") ?: "Rappel Nexora"
        val frequence = intent.getStringExtra("frequence") ?: "UNIQUE"
        val joursTexte = intent.getStringExtra("jours") ?: ""

        val gestionnaireNotif = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "nexora_rappels",
                "Rappels Nexora",
                NotificationManager.IMPORTANCE_HIGH
            )
            gestionnaireNotif.createNotificationChannel(canal)
        }

        val notification = NotificationCompat.Builder(context, "nexora_rappels")
            .setContentTitle("Nexora")
            .setContentText(titre)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        gestionnaireNotif.notify(titre.hashCode(), notification)

        val gestionnaireAlarme = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance()
        val heure = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        var prochainDeclenchement: Long? = null

        when (frequence) {
            "QUOTIDIEN" -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                prochainDeclenchement = cal.timeInMillis
            }
            "HEBDOMADAIRE" -> {
                cal.add(Calendar.DAY_OF_YEAR, 7)
                prochainDeclenchement = cal.timeInMillis
            }
            "PERSONNALISE" -> {
                val joursAutorises = joursTexte.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                if (joursAutorises.isNotEmpty()) {
                    val calSuivant = Calendar.getInstance()
                    calSuivant.set(Calendar.HOUR_OF_DAY, heure)
                    calSuivant.set(Calendar.MINUTE, minute)
                    calSuivant.set(Calendar.SECOND, 0)
                    for (i in 1..7) {
                        calSuivant.add(Calendar.DAY_OF_YEAR, 1)
                        if (joursAutorises.contains(calSuivant.get(Calendar.DAY_OF_WEEK))) {
                            prochainDeclenchement = calSuivant.timeInMillis
                            break
                        }
                    }
                }
            }
        }

        if (prochainDeclenchement != null) {
            val nouvelleIntention = Intent(context, RappelReceiver::class.java).apply {
                putExtra("titre", titre)
                putExtra("frequence", frequence)
                putExtra("jours", joursTexte)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                titre.hashCode(),
                nouvelleIntention,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            gestionnaireAlarme.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                prochainDeclenchement,
                pendingIntent
            )
        }
    }
}
