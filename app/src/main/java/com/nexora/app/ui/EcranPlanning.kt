package com.nexora.app.ui

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
import androidx.compose.ui.unit.dp
import com.nexora.app.data.chargerPlanning
import com.nexora.app.data.sauvegarderPlanning
import com.nexora.app.model.BlocHoraire

private val VioletNexora = Color(0xFF7B5CFF)
private val JoursSemaine = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")
private val HeuresJournee = (6..22).toList()

fun heureVersMinutes(heure: String): Int {
    return try {
        val nettoyee = heure.replace("H", "h")
        val parties = nettoyee.split("h")
        val h = parties[0].trim().toInt()
        val m = if (parties.size > 1 && parties[1].isNotBlank()) parties[1].trim().toInt() else 0
        h * 60 + m
    } catch (e: Exception) {
        0
    }
}

fun genererPlanningParDefaut(): List<BlocHoraire> {
    val joursCours = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi")
    return joursCours.map { jour ->
        BlocHoraire(jour = jour, heureDebut = "07h00", heureFin = "18h00", activite = "Cours")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EcranPlanning() {
    val context = LocalContext.current
    var planning by remember {
        mutableStateOf(
            chargerPlanning(context).ifEmpty {
                val parDefaut = genererPlanningParDefaut()
                sauvegarderPlanning(context, parDefaut)
                parDefaut
            }
        )
    }
    var indexJour by remember { mutableStateOf(0) }
    var formulaireOuvert by remember { mutableStateOf(false) }
    var blocASupprimer by remember { mutableStateOf<BlocHoraire?>(null) }

    val jourActuel = JoursSemaine[indexJour]
    val blocsDuJour = planning.filter { it.jour == jourActuel }.sortedBy { heureVersMinutes(it.heureDebut) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Mon emploi du temps", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = { formulaireOuvert = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletNexora)
            ) {
                Text("+ Ajouter")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { indexJour = (indexJour - 1 + 7) % 7 }) { Text("◀") }
            Text(text = jourActuel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = VioletNexora)
            TextButton(onClick = { indexJour = (indexJour + 1) % 7 }) { Text("▶") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Astuce : appui long sur un bloc pour le supprimer", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(HeuresJournee) { heure ->
                val blocPourCetteHeure = blocsDuJour.firstOrNull { bloc ->
                    val debut = heureVersMinutes(bloc.heureDebut)
                    val fin = heureVersMinutes(bloc.heureFin)
                    val minuteActuelle = heure * 60
                    minuteActuelle in debut until fin
                }

                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "%02dh".format(heure),
                        modifier = Modifier.width(48.dp),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (blocPourCetteHeure != null) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = VioletNexora.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(vertical = 2.dp)
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {},
                                    onLongClick = { blocASupprimer = blocPourCetteHeure }
                                )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "${blocPourCetteHeure.activite} (${blocPourCetteHeure.heureDebut}-${blocPourCetteHeure.heureFin})",
                                    color = VioletNexora,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight())
                    }
                }
            }
        }
    }

    if (formulaireOuvert) {
        var jourChoisi by remember { mutableStateOf(jourActuel) }
        var heureDebut by remember { mutableStateOf("") }
        var heureFin by remember { mutableStateOf("") }
        var activite by remember { mutableStateOf("") }
        var menuJourOuvert by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { formulaireOuvert = false },
            title = { Text("Nouveau bloc") },
            text = {
                Column {
                    Box {
                        OutlinedButton(onClick = { menuJourOuvert = true }, shape = RoundedCornerShape(12.dp)) {
                            Text(jourChoisi)
                        }
                        DropdownMenu(expanded = menuJourOuvert, onDismissRequest = { menuJourOuvert = false }) {
                            JoursSemaine.forEach { j ->
                                DropdownMenuItem(text = { Text(j) }, onClick = { jourChoisi = j; menuJourOuvert = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = activite, onValueChange = { activite = it }, label = { Text("Activité") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = heureDebut, onValueChange = { heureDebut = it }, label = { Text("Heure début (ex: 14h00)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = heureFin, onValueChange = { heureFin = it }, label = { Text("Heure fin (ex: 15h30)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (activite.isNotBlank() && heureDebut.isNotBlank() && heureFin.isNotBlank()) {
                        planning = planning + BlocHoraire(jourChoisi, heureDebut, heureFin, activite)
                        sauvegarderPlanning(context, planning)
                        formulaireOuvert = false
                    }
                }) { Text("Ajouter") }
            },
            dismissButton = {
                TextButton(onClick = { formulaireOuvert = false }) { Text("Annuler") }
            }
        )
    }

    blocASupprimer?.let { bloc ->
        AlertDialog(
            onDismissRequest = { blocASupprimer = null },
            title = { Text("Supprimer ce bloc ?") },
            text = { Text("« ${bloc.activite} » (${bloc.jour}, ${bloc.heureDebut}-${bloc.heureFin}) sera supprimé.") },
            confirmButton = {
                TextButton(onClick = {
                    planning = planning.filter { it !== bloc }
                    sauvegarderPlanning(context, planning)
                    blocASupprimer = null
                }) { Text("Supprimer", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { blocASupprimer = null }) { Text("Annuler") }
            }
        )
    }
}
