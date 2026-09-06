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
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRetour) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = Color(0xFF2A2438))
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = requete,
                    onValueChange = { requete = it },
                    placeholder = { Text("Rechercher une tâche ou une note...") },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                if (requete.isBlank()) {
                    item {
                        Text("Tape un mot-clé pour chercher parmi tes tâches et tes notes", color = Color.Gray)
                    }
                } else if (resultats.isEmpty()) {
                    item {
                        Text("Aucun résultat pour « $requete »", color = Color.Gray)
                    }
                } else {
                    items(resultats) { resultat ->
                        when (resultat) {
                            is ResultatRecherche.ResultatTache -> {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = BlancLait),
                                    modifier = Modifier.fillMaxWidth().clickable { tacheEnEdition = resultat.tache }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VioletKrenoa, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(resultat.tache.titre, fontWeight = FontWeight.Medium)
                                            Text(resultat.tache.rappel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                            is ResultatRecherche.ResultatNote -> {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = BlancLait),
                                    modifier = Modifier.fillMaxWidth().clickable { noteAConvertir = resultat.texte }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Description, contentDescription = null, tint = VioletKrenoa, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(resultat.texte, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    tacheEnEdition?.let { tache ->
        FormulaireTache(
            titre = "Modifier la tâche",
            tacheExistante = tache,
            onFermer = { tacheEnEdition = null },
            onValider = { titreV, rappelV, millisV, prioriteV, frequenceV, joursV ->
                val modifiee = tache.copy(titre = titreV, rappel = rappelV, rappelMillis = millisV, priorite = prioriteV, frequence = frequenceV, joursRepetition = joursV)
                val misesAJour = taches.map { if (it == tache) modifiee else it }
                sauvegarderTaches(context, misesAJour)
                taches = misesAJour
                programmerAlarme(context, modifiee)
                tacheEnEdition = null
            }
        )
    }

    noteAConvertir?.let { note ->
        FormulaireTache(
            titre = "Créer une tâche",
            tacheExistante = Tache(titre = note),
            onFermer = { noteAConvertir = null },
            onValider = { titreV, rappelV, millisV, prioriteV, frequenceV, joursV ->
                val nouvelle = Tache(titreV, rappel = rappelV, rappelMillis = millisV, priorite = prioriteV, frequence = frequenceV, joursRepetition = joursV)
                val actuelles = chargerTaches(context)
                val misesAJour = actuelles + nouvelle
                sauvegarderTaches(context, misesAJour)
                taches = misesAJour
                programmerAlarme(context, nouvelle)
                idees = idees.filter { it != note }
                sauvegarderIdees(context, idees)
                noteAConvertir = null
            }
        )
    }
}
