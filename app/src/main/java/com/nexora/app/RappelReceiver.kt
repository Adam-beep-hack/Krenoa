package com.nexora.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class RappelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titre = intent.getStringExtra("titre") ?: "Rappel Nexora"
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
    }
}
