package com.nexora.app.model

import androidx.compose.ui.graphics.Color
import java.util.Calendar

enum class Priorite(val libelle: String, val couleur: Color) {
    URGENTE("Urgente", Color(0xFFE53935)),
    ELEVEE("Élevée", Color(0xFFFB8C00)),
    MOYENNE("Moyenne", Color(0xFFFDD835)),
    FAIBLE("Faible", Color(0xFF43A047))
}

enum class Frequence(val libelle: String) {
    UNIQUE("Ne pas répéter"),
    QUOTIDIEN("Tous les jours"),
    HEBDOMADAIRE("Toutes les semaines"),
    PERSONNALISE("Jours précis")
}

val NomsJours = listOf(
    Calendar.SUNDAY to "Dim",
    Calendar.MONDAY to "Lun",
    Calendar.TUESDAY to "Mar",
    Calendar.WEDNESDAY to "Mer",
    Calendar.THURSDAY to "Jeu",
    Calendar.FRIDAY to "Ven",
    Calendar.SATURDAY to "Sam"
)

data class Tache(
    val titre: String,
    val terminee: Boolean = false,
    val rappel: String = "",
    val rappelMillis: Long = 0L,
    val priorite: Priorite = Priorite.MOYENNE,
    val frequence: Frequence = Frequence.UNIQUE,
    val joursRepetition: Set<Int> = emptySet()
)
