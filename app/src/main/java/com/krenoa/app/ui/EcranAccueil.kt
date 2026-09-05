@file:OptIn(ExperimentalFoundationApi::class)
package com.krenoa.app.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krenoa.app.data.chargerIdees
import com.krenoa.app.data.chargerTaches
import com.krenoa.app.data.programmerAlarme
import com.krenoa.app.data.sauvegarderIdees
import com.krenoa.app.data.sauvegarderTaches
import com.krenoa.app.model.Tache

private val VioletKrenoa = Color(0xFF7B5CFF)
private val OrKrenoa = Color(0xFFF6B93B)
private val FondGris = Color(0xFFF5F4FA)
private val VertFaible = Color(0xFF43A047)
private val RougeUrgent = Color(0xFFE53935)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EcranAccueil(taches: List<Tache>) {
    val context = LocalContext.current
    var listeTaches by remember { mutableStateOf(taches) }
    val total = listeTaches.size
    val terminees = listeTaches.count { it.terminee }
    val progression = if (total > 0) (terminees * 100 / total) else 0

    val prochainsRappels = listeTaches
        .filter { it.rappelMillis > System.currentTimeMillis() && !it.terminee }
        .sortedBy { it.rappelMillis }
        .take(2)

    var idees by remember { mutableStateOf(chargerIdees(context)) }
    var texteIdee by remember { mutableStateOf("") }
    var ideeASupprimer by remember { mutableStateOf<String?>(null) }
    var ideeAConvertir by remember { mutableStateOf<String?>(null) }
    var afficherAjoutRapide by remember { mutableStateOf(false) }

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

    fun bascculerTermine(tache: Tache) {
        listeTaches = listeTaches.map {
            if (it == tache) it.copy(terminee = !it.terminee) else it
        }
        sauvegarderTaches(context, listeTaches)
    }

    Box(modifier = Modifier.fillMaxSize().background(FondGris)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color(0xFF2A2438))
                    Text("KRENOA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2A2438))
                    Row {
                        Icon(Icons.Filled.Search, contentDescription = "Rechercher", tint = Color(0xFF2A2438))
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color(0xFF2A2438))
                    }
                }
            }

            item {
                Column {
                    Text("Bonjour, Hama ! 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Prêt à accomplir de grandes choses aujourd'hui ?", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF120E24)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Progression quotidienne", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tu es sur la bonne voie !", color = Color(0xFFB9A6FF), style = MaterialTheme.typography.bodySmall)
                            Text("$terminees / $total tâches terminées", color = Color(0xFFB9A6FF), style = MaterialTheme.typography.bodySmall)
                        }
                        AnneauProgression(pourcentage = progression)
                    }
                }
            }

             item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mes tâches du jour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Voir tout", color = VioletKrenoa, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        val tachesDuJour = listeTaches.filter { !it.terminee }.take(5)
                        if (tachesDuJour.isEmpty()) {
                            Text("Aucune tâche pour aujourd'hui", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            tachesDuJour.forEachIndexed { index, tache ->
                                LigneTache(tache = tache, surCoche = { bascculerTermine(tache) })
                                if (index != tachesDuJour.lastIndex) {
                                    Divider(color = Color(0xFFF0EEF7))
                                }
                            }
                        }

                        TextButton(
                            onClick = { afficherAjoutRapide = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = VioletKrenoa)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ajouter une tâche", color = VioletKrenoa, fontWeight = FontWeight.Medium)
                        }
                    }
                }
             }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Prochains rappels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Voir tout", color = VioletKrenoa, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (prochainsRappels.isEmpty()) {
                            Text("Aucun rappel à venir", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        } else {
                            prochainsRappels.forEachIndexed { index, tache ->
                                LigneRappel(tache = tache)
                                if (index != prochainsRappels.lastIndex) {
                                    Divider(color = Color(0xFFF0EEF7))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = OrKrenoa)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Boîte à idées", fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier.size(24.dp).clip(CircleShape).background(FondGris),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${idees.size}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = texteIdee,
                                onValueChange = { texteIdee = it },
                                label = { Text("Nouvelle idée") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = { lancerDicteeVocale() }, shape = RoundedCornerShape(50)) {
                                Text("🎤")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = {
                                    if (texteIdee.isNotBlank()) {
                                        idees = idees + texteIdee
                                        sauvegarderIdees(context, idees)
                                        texteIdee = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrKrenoa)
                            ) { Text("+") }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Astuce : appui simple pour créer une tâche, appui long pour supprimer", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            items(idees) { idee ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = OrKrenoa.copy(alpha = 0.12f)),
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

        FloatingActionButton(
            onClick = { afficherAjoutRapide = true },
            containerColor = VioletKrenoa,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Ajouter une tâche", tint = Color.White)
        }
    }

    if (afficherAjoutRapide) {
        FormulaireTache(
            titre = "Nouvelle tâche",
            tacheExistante = null,
            onFermer = { afficherAjoutRapide = false },
            onValider = { titreV, rappelV, millisV, prioriteV, frequenceV, joursV ->
                val nouvelle = Tache(titreV, rappel = rappelV, rappelMillis = millisV, priorite = prioriteV, frequence = frequenceV, joursRepetition = joursV)
                val actuelles = chargerTaches(context)
                val misesAJour = actuelles + nouvelle
                sauvegarderTaches(context, misesAJour)
                listeTaches = misesAJour
                programmerAlarme(context, nouvelle)
                afficherAjoutRapide = false
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
                listeTaches = misesAJour
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

@Composable
private fun AnneauProgression(pourcentage: Int) {
    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val epaisseur = 8.dp.toPx()
            drawArc(
                color = Color(0xFF2A2438),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = epaisseur, cap = StrokeCap.Round)
            )
            drawArc(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(VioletKrenoa, OrKrenoa)),
                startAngle = -90f,
                sweepAngle = 360f * (pourcentage / 100f),
                useCenter = false,
                style = Stroke(width = epaisseur, cap = StrokeCap.Round)
            )
        }
        Text("$pourcentage%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun LigneTache(tache: Tache, surCoche: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = tache.terminee, onCheckedChange = { surCoche() }, colors = CheckboxDefaults.colors(checkedColor = VioletKrenoa))
            Column {
                Text(tache.titre, fontWeight = FontWeight.Medium)
                Text(tache.rappel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(tache.priorite.couleur.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(tache.priorite.libelle, style = MaterialTheme.typography.labelSmall, color = tache.priorite.couleur)
        }
    }
}

@Composable
private fun LigneRappel(tache: Tache) {
    val diffMillis = tache.rappelMillis - System.currentTimeMillis()
    val heures = diffMillis / (1000 * 60 * 60)
    val minutes = (diffMillis / (1000 * 60)) % 60
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(tache.titre, fontWeight = FontWeight.Medium)
            Text(tache.rappel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text("Dans ${heures}h ${minutes}m", color = VioletKrenoa, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
    }
}
