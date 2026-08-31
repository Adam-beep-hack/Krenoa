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

        if (frequence == "QUOTIDIEN" || frequence == "HEBDOMADAIRE") {
            val gestionnaireAlarme = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val cal = Calendar.getInstance()
            if (frequence == "QUOTIDIEN") {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            } else {
                cal.add(Calendar.DAY_OF_YEAR, 7)
            }
            val nouvelleIntention = Intent(context, RappelReceiver::class.java).apply {
                putExtra("titre", titre)
                putExtra("frequence", frequence)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                titre.hashCode(),
                nouvelleIntention,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            gestionnaireAlarme.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )
        }
    }
}
