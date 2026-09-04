package com.nexora.app.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexora.app.data.chargerIdees
import com.nexora.app.data.chargerTaches
import com.nexora.app.data.programmerAlarme
import com.nexora.app.data.sauvegarderIdees
import com.nexora.app.data.sauvegarderTaches
import com.nexora.app.model.Tache

private val VioletNexora = Color(0xFF7B5CFF)
private val OrNexora = Color(0xFFF6B93B)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EcranAccueil(taches: List<Tache>) {
    val context = LocalContext.current
    val total = taches.size
    val terminees = taches.count { it.terminee }
    val progression = if (total > 0) (terminees * 100 / total) else 0
    val prochainRappel = taches.filter { it.rappelMillis > 0 && !it.terminee }.minByOrNull { it.rappelMillis }

    var idees by remember { mutableStateOf(chargerIdees(context)) }
    var texteIdee by remember { mutableStateOf("") }
    var ideeASupprimer by remember { mutableStateOf<String?>(null) }
    var ideeAConvertir by remember { mutableStateOf<String?>(null) }

    val lanceurVocal = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultat ->
        if (resultat.resultCode == Activity.RESULT_OK) {
            val texteReconnu = resultat.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!texteReconnu.isNullOrBlank()) {
                texteIdee = texteReconnu
            }
        }
    }

    fun lancerDicteeVocale() {
        val intention = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Dicte ton idée")
        }
        lanceurVocal.launch(intention)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "NEXORA", style = MaterialTheme.typography.labelLarge, color = VioletNexora, fontWeight = FontWeight.Bold)
            Text(text = "Bonjour !", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VioletNexora),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Progression du jour", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$progression%", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text(text = "$terminees / $total tâches terminées", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Prochain rappel", fontWeight = FontWeight.Bold, color = OrNexora)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (prochainRappel != null) {
                        Text(text = prochainRappel.titre, style = MaterialTheme.typography.bodyLarge)
                        Text(text = prochainRappel.rappel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        Text(text = "Aucun rappel à venir", color = Color.Gray)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$total", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = VioletNexora)
                        Text(text = "Tâches totales", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${total - terminees}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = OrNexora)
                        Text(text = "Restantes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        item {
            Text(text = "Boîte à idées", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Note ou dicte une idée, puis touche-la pour en faire une tâche", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = texteIdee,
                    onValueChange = { texteIdee = it },
                    label = { Text("Nouvelle idée") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { lancerDicteeVocale() }) {
                    Icon(Icons.Filled.Mic, contentDescription = "Dicter", tint = VioletNexora)
                }
                Button(
                    onClick = {
                        if (texteIdee.isNotBlank()) {
                            idees = idees + texteIdee
                            sauvegarderIdees(context, idees)
                            texteIdee = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrNexora)
                ) {
                    Text("+")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Astuce : appui simple pour créer une tâche, appui long pour supprimer", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        items(idees) { idee ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = OrNexora.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { ideeAConvertir = idee },
                        onLongClick = { ideeASupprimer = idee }
                    )
            ) {
                Text(text = idee, modifier = Modifier.padding(12.dp))
            }
        }
    }

    ideeAConvertir?.let { idee ->
        FormulaireTache(
            titre = "Créer une tâche",
            tacheExistante = Tache(titre = idee),
            onFermer = { ideeAConvertir = null },
            onValider = { titreV, rappelV, millisV, prioriteV, frequenceV, joursV ->
                val nouvelle = Tache(titreV, rappel = rappelV, rappelMillis = millisV, priorite = prioriteV, frequence = frequenceV, joursRepetition = joursV)
                val tachesActuelles = chargerTaches(context)
                sauvegarderTaches(context, tachesActuelles + nouvelle)
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
            title = { Text("Supprimer cette idée ?") },
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
