package com.krenoa.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
private val OrKrenoa = Color(0xFFF6B93B)
private val FondGris = Color(0xFFF5F4FA)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun EcranToutesNotes(onRetour: () -> Unit) {
    val context = LocalContext.current
    var idees by remember { mutableStateOf(chargerIdees(context)) }
    var ideeASupprimer by remember { mutableStateOf<String?>(null) }
    var ideeAConvertir by remember { mutableStateOf<String?>(null) }
    var afficherAjoutNote by remember { mutableStateOf(false) }
    var texteNouvelleNote by remember { mutableStateOf("") }

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
                Text("Mes notes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                if (idees.isEmpty()) {
                    item {
                        Text("Aucune note pour le moment", color = Color.Gray)
                    }
                } else {
                    items(idees.reversed()) { idee ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { ideeAConvertir = idee },
                                    onLongClick = { ideeASupprimer = idee }
                                )
                        ) {
                            Text(text = idee, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { afficherAjoutNote = true },
            containerColor = OrKrenoa,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Ajouter une note", tint = Color.White)
        }
    }

    if (afficherAjoutNote) {
        AlertDialog(
            onDismissRequest = { afficherAjoutNote = false; texteNouvelleNote = "" },
            title = { Text("Nouvelle note") },
            text = {
                OutlinedTextField(
                    value = texteNouvelleNote,
                    onValueChange = { texteNouvelleNote = it },
                    label = { Text("Écris ta note") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (texteNouvelleNote.isNotBlank()) {
                        idees = idees + texteNouvelleNote
                        sauvegarderIdees(context, idees)
                        texteNouvelleNote = ""
                    }
                    afficherAjoutNote = false
                }) { Text("Enregistrer", color = OrKrenoa, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { afficherAjoutNote = false; texteNouvelleNote = "" }) { Text("Annuler") }
            }
        )
    }

    ideeAConvertir?.let { idee ->
        FormulaireTache(
            titre = "Créer une tâche",
            tacheExistante = Tache(titre = idee),
            onFermer = { ideeAConvertir = null },
            onValider = { titreV, rappelV, millisV, prioriteV, frequenceV, joursV ->
                val nouvelle = Tache(titreV, rappel = rappelV, rappelMillis = millisV, priorite = prioriteV, frequence = frequenceV, joursRepetition = joursV)
                val actuelles = chargerTaches(context)
                val misesAJour = actuelles + nouvelle
                sauvegarderTaches(context, misesAJour)
                programmerAlarme(context, nouvelle)
                idees = idees.filter { it != idee }
                sauvegarderIdees(context, idees)
                ideeAConvertir = null
            }
        )
    }

    ideeASupprimer?.let { idee ->
        AlertDialog(
            onDismissRequest = { ideeASupprimer = null },
            title = { Text("Supprimer cette note ?") },
            text = { Text("« $idee » sera supprimée.") },
            confirmButton = {
                TextButton(onClick = {
                    idees = idees.filter { it != idee }
                    sauvegarderIdees(context, idees)
                    ideeASupprimer = null
                }) { Text("Supprimer", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { ideeASupprimer = null }) { Text("Annuler") }
            }
        )
    }
}
