@file:OptIn(ExperimentalFoundationApi::class)
package com.nexora.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nexora.app.data.chargerTaches
import com.nexora.app.data.programmerAlarme
import com.nexora.app.data.sauvegarderTaches
import com.nexora.app.model.Frequence
import com.nexora.app.model.NomsJours
import com.nexora.app.model.Priorite
import com.nexora.app.model.Tache
import java.util.Calendar

private val VioletNexora = Color(0xFF7B5CFF)

@Composable
fun FormulaireTache(
    titre: String,
    onFermer: () -> Unit,
    onValider: (String, String, Long, Priorite, Frequence, Set<Int>) -> Unit,
    tacheExistante: Tache? = null
) {
    val context = LocalContext.current
    var texteTitre by remember { mutableStateOf(tacheExistante?.titre ?: "") }
    var rappelTexte by remember { mutableStateOf(tacheExistante?.rappel ?: "") }
    var rappelMillis by remember { mutableStateOf(tacheExistante?.rappelMillis ?: 0L) }
    var priorite by remember { mutableStateOf(tacheExistante?.priorite ?: Priorite.MOYENNE) }
    var frequence by remember { mutableStateOf(tacheExistante?.frequence ?: Frequence.UNIQUE) }
    var joursChoisis by remember { mutableStateOf(tacheExistante?.joursRepetition ?: emptySet()) }
    var menuPrioriteOuvert by remember { mutableStateOf(false) }
    var menuFrequenceOuvert by remember { mutableStateOf(false) }

    fun ouvrirSelecteurDateHeure() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, annee, mois, jour ->
                TimePickerDialog(
                    context,
                    { _, heure, minute ->
                        val calChoisi = Calendar.getInstance()
                        calChoisi.set(annee, mois, jour, heure, minute, 0)
                        rappelMillis = calChoisi.timeInMillis
                        rappelTexte = "%02d/%02d/%d à %02dh%02d".format(jour, mois + 1, annee, heure, minute)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text(titre) },
        text = {
            Column {
                OutlinedTextField(
                    value = texteTitre,
                    onValueChange = { texteTitre = it },
                    label = { Text("Titre") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { ouvrirSelecteurDateHeure() }, shape = RoundedCornerShape(12.dp)) {
                    Text(if (rappelTexte.isBlank()) "Date/heure" else rappelTexte)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(
                        onClick = { menuPrioriteOuvert = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = priorite.couleur)
                    ) {
                        Text(priorite.libelle)
                    }
                    DropdownMenu(expanded = menuPrioriteOuvert, onDismissRequest = { menuPrioriteOuvert = false }) {
                        Priorite.values().forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.libelle, color = p.couleur) },
                                onClick = { priorite = p; menuPrioriteOuvert = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { menuFrequenceOuvert = true }, shape = RoundedCornerShape(12.dp)) {
                        Text(frequence.libelle)
                    }
                    DropdownMenu(expanded = menuFrequenceOuvert, onDismissRequest = { menuFrequenceOuvert = false }) {
                        Frequence.values().forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f.libelle) },
                                onClick = { frequence = f; menuFrequenceOuvert = false }
                            )
                        }
                    }
                }
                if (frequence == Frequence.PERSONNALISE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Choisir les jours :", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        NomsJours.forEach { (valeurJour, nomJour) ->
                            val selectionne = joursChoisis.contains(valeurJour)
                            FilterChip(
                                selected = selectionne,
                                onClick = {
                                    joursChoisis = if (selectionne) joursChoisis - valeurJour else joursChoisis + valeurJour
                                },
                                label = { Text(nomJour) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (texteTitre.isNotBlank()) {
                    onValider(texteTitre, rappelTexte, rappelMillis, priorite, frequence, joursChoisis)
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onFermer) { Text("Annuler") }
        }
    )
}

@Composable
fun EcranTaches() {
    val context = LocalContext.current
    var taches by remember { mutableStateOf(chargerTaches(context)) }
    var tacheASupprimer by remember { mutableStateOf<Tache?>(null) }
    var tacheAModifier by remember { mutableStateOf<Tache?>(null) }
    var ajoutOuvert by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "NEXORA", style = MaterialTheme.typography.labelLarge, color = VioletNexora, fontWeight = FontWeight.Bold)
                Text(text = "Mes tâches", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { ajoutOuvert = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletNexora)
            ) {
                Text("+ Ajouter")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Astuce : appui simple pour modifier, appui long pour supprimer", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(taches) { tache ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { tacheAModifier = tache },
                            onLongClick = { tacheASupprimer = tache }
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.padding(end = 8.dp).size(10.dp)) {
                            Surface(color = tache.priorite.couleur, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxSize()) {}
                        }
                        Checkbox(
                            checked = tache.terminee,
                            onCheckedChange = { coche ->
                                taches = taches.map { if (it === tache) it.copy(terminee = coche) else it }
                                sauvegarderTaches(context, taches)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = VioletNexora)
                        )
                        Column {
                            Text(text = tache.titre, textDecoration = if (tache.terminee) TextDecoration.LineThrough else null)
                            val joursTexte = if (tache.frequence == Frequence.PERSONNALISE && tache.joursRepetition.isNotEmpty()) {
                                NomsJours.filter { tache.joursRepetition.contains(it.first) }.joinToString(",") { it.second }
                            } else null
                            val details = listOfNotNull(
                                tache.rappel.takeIf { it.isNotBlank() },
                                joursTexte ?: tache.frequence.libelle.takeIf { tache.frequence != Frequence.UNIQUE }
                            ).joinToString(" · ")
                            if (details.isNotBlank()) {
                                Text(text = details, style = MaterialTheme.typography.bodySmall, color = VioletNexora)
                            }
                        }
                    }
                }
            }
        }
    }

    if (ajoutOuvert) {
        FormulaireTache(
            titre = "Nouvelle tâche",
            onFermer = { ajoutOuvert = false },
            onValider = { titreV, rappelV, millisV, prioriteV, frequenceV, joursV ->
                val nouvelle = Tache(titreV, rappel = rappelV, rappelMillis = millisV, priorite = prioriteV, frequence = frequenceV, joursRepetition = joursV)
                taches = taches + nouvelle
                sauvegarderTaches(context, taches)
                programmerAlarme(context, nouvelle)
                ajoutOuvert = false
            }
        )
    }

    tacheAModifier?.let { tache ->
        FormulaireTache(
            titre = "Modifier la tâche",
            tacheExistante = tache,
            onFermer = { tacheAModifier = null },
            onValider = { titreV, rappelV, millisV, prioriteV, frequenceV, joursV ->
                taches = taches.map {
                    if (it === tache) it.copy(titre = titreV, rappel = rappelV, rappelMillis = millisV, priorite = prioriteV, frequence = frequenceV, joursRepetition = joursV) else it
                }
                sauvegarderTaches(context, taches)
                val tacheMaj = taches.first { it.titre == titreV }
                programmerAlarme(context, tacheMaj)
                tacheAModifier = null
            }
        )
    }

    tacheASupprimer?.let { tache ->
        AlertDialog(
            onDismissRequest = { tacheASupprimer = null },
            title = { Text("Supprimer la tâche ?") },
            text = { Text("« ${tache.titre} » sera définitivement supprimée.") },
            confirmButton = {
                TextButton(onClick = {
                    taches = taches.filter { it !== tache }
                    sauvegarderTaches(context, taches)
                    tacheASupprimer = null
                }) { Text("Supprimer", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { tacheASupprimer = null }) { Text("Annuler") }
            }
        )
    }
}
