package com.nexora.app.data

import android.content.Context
import com.nexora.app.model.BlocHoraire


fun sauvegarderPlanning(
    context: Context,
    planning: List<BlocHoraire>
) {

    val prefs = context.getSharedPreferences(
        "nexora_prefs",
        Context.MODE_PRIVATE
    )

    val texte = planning.joinToString(";;") {

        "${it.jour}~${it.heureDebut}~${it.heureFin}~${it.activite}"

    }

    prefs.edit()
        .putString("planning", texte)
        .apply()
}



fun chargerPlanning(
    context: Context
): List<BlocHoraire> {

    val prefs = context.getSharedPreferences(
        "nexora_prefs",
        Context.MODE_PRIVATE
    )


    val texte = prefs.getString(
        "planning",
        ""
    ) ?: ""


    if (texte.isBlank()) {
        return emptyList()
    }


    return texte.split(";;")
        .mapNotNull { ligne ->

            val parties = ligne.split("~")


            if (parties.size == 4) {

                BlocHoraire(
                    jour = parties[0],
                    heureDebut = parties[1],
                    heureFin = parties[2],
                    activite = parties[3]
                )

            } else {
                null
            }
        }
}
