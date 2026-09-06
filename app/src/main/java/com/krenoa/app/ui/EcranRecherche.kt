package com.krenoa.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krenoa.app.data.chargerIdees
import com.krenoa.app.data.chargerTaches
import com.krenoa.app.data.programmerAlarme
import com.krenoa.app.data.sauvegarderIdees
import com.krenoa.app.data.sauvegarderTaches
import com.krenoa.app.model.Tache

private val VioletKrenoa = Color(0xFF7B5CFF)
private val FondGris = Color(0xFFF5F4FA)
private val BlancLait = Color(0xFFFFFBF2)

private sealed class ResultatRecherche {
    data class ResultatTache(val tache: Tache) : ResultatRecherche()
    data class ResultatNote(val texte: String) : ResultatRecherche()
}

@Composable
fun EcranRecherche(onRetour: () -> Unit) {
    val context = LocalContext.current
    var requete by remember { mutableStateOf("") }
    var taches by remember { mutableStateOf(chargerTaches(context)) }
    var idees by remember { mutableStateOf(chargerIdees(context)) }
    var tacheEnEdition by remember { mutableStateOf<Tache?>(null) }
    var noteAConvertir by remember { mutableStateOf<String?>(null) }

    val resultats = remember(requete, taches, idees) {
        if (requete.isBlank()) {
            emptyList()
        } else {
            val q = requete.trim().lowercase()
            val tachesTrouvees = taches
                .filter { it.titre.lowercase().contains(q) }
                .map { ResultatRecherche.ResultatTache(it) }
            val notesTrouvees = idees
                .filter { it.lowercase().contains(q) }
                .map { ResultatRecherche.ResultatNote(it) }
            tachesTrouvees + notesTrouvees
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(FondGris)) {
        Column(modifier = Modifier.fillMaxSize()
