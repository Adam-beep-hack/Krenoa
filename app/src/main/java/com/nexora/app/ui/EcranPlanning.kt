package com.nexora.app.ui

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
import androidx.compose.ui.unit.dp
import com.nexora.app.data.chargerPlanning
import com.nexora.app.data.sauvegarderPlanning
import com.nexora.app.model.BlocHoraire
import java.util.Calendar

private val VioletNexora = Color(0xFF7B5CFF)
private val JoursSemaine = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")

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
            Text(text = "Mon emploi du temps", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            FloatingActionButton(
                onClick = { formulaireOuvert = true },
                containerColor = VioletNexora,
                shape = RoundedCornerShape(50)
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        if (blocsDuJour.isEmpty()) {
            Text(text = "Rien de prévu ce jour", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(blocsDuJour) { bloc ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VioletNexora.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                                onLongClick = { blocASupprimer = bloc }
                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = bloc.activite, fontWeight = FontWeight.Bold, color = VioletNexora)
                            Text(text = "${bloc.heureDebut} - ${bloc.heureFin}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
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

        fun choisirHeure(surChoisi: (String) -> Unit) {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, h, m -> surChoisi("%02dh%02d".format(h, m)) },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

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
                    OutlinedButton(
                        onClick = { choisirHeure { heureDebut = it } },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (heureDebut.isBlank()) "Choisir l'heure de début" else "Début : $heureDebut")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { choisirHeure { heureFin = it } },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (heureFin.isBlank()) "Choisir l'heure de fin" else "Fin : $heureFin")
                    }
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
