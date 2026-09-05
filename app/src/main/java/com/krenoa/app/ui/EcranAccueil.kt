package com.krenoa.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EcranAccueil(taches: List<Tache>, onVoirToutTaches: () -> Unit = {}) {
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
    var ideeASupprimer by remember { mutableStateOf<String?>(null) }
    var ideeAConvertir by remember { mutableStateOf<String?>(null) }
    var afficherAjoutRapide by remember { mutableStateOf(false) }
    var afficherAjoutNote by remember { mutableStateOf(false) }
    var texteNouvelleNote by remember { mutableStateOf("") }

    fun bascculerTermine(tache: Tache) {
        listeTaches = listeTaches.map {
            if (it == tache) it.copy(terminee = !it.terminee) else it
        }
        sauvegarderTaches(context, listeTaches)
    }

    Box(modifier = Modifier.fillMaxSize().background(FondGris)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color(0xFF2A2438), modifier = Modifier.size(20.dp))
                Text("KRENOA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2A2438))
                Row {
                    Icon(Icons.Filled.Search, contentDescription = "Rechercher", tint = Color(0xFF2A2438), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color(0xFF2A2438), modifier = Modifier.size(20.dp))
                }
            }

            Column {
                Text("Bonjour, Hama ! 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Prêt à accomplir de grandes choses aujourd'hui ?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF120E24)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Progression quotidienne", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tu es sur la bonne voie !", color = Color(0xFFB9A6FF), style = MaterialTheme.typography.labelSmall)
                        Text("$terminees / $total tâches terminées", color = Color(0xFFB9A6FF), style = MaterialTheme.typography.labelSmall)
                    }
                    AnneauProgression(pourcentage = progression)
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mes tâches du jour", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Voir tout",
                            color = VioletKrenoa,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onVoirToutTaches() }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    val tachesDuJour = listeTaches.filter { !it.terminee }.take(2)
                    if (tachesDuJour.isEmpty()) {
                        Text("Aucune tâche pour aujourd'hui", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
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
                        contentPadding = PaddingValues(vertical = 2.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = VioletKrenoa, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter une tâche", color = VioletKrenoa, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Prochains rappels", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Voir tout", color = VioletKrenoa, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    if (prochainsRappels.isEmpty()) {
                        Text("Aucun rappel à venir", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
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

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mes notes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Voir tout", color = VioletKrenoa, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    val dernieresNotes = idees.takeLast(2).reversed()
                    if (dernieresNotes.isEmpty()) {
                        Text("Aucune note pour le moment", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    } else {
                        dernieresNotes.forEachIndexed { index, idee ->
                            Text(
                                text = idee,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { ideeAConvertir = idee },
                                        onLongClick = { ideeASupprimer = idee }
                                    )
                                    .padding(vertical = 6.dp)
                            )
                            if (index != dernieresNotes.lastIndex) {
                                Divider(color = Color(0xFFF0EEF7))
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { afficherAjoutNote = true },
            containerColor = OrKrenoa,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Ajouter une note", tint = Color.White)
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

@Composable
private fun AnneauProgression(pourcentage: Int) {
    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val epaisseur = 7.dp.toPx()
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
        Text("$pourcentage%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun LigneTache(tache: Tache, surCoche: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = tache.terminee,
                onCheckedChange = { surCoche() },
                colors = CheckboxDefaults.colors(checkedColor = VioletKrenoa),
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(tache.titre, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                Text(tache.rappel, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(tache.priorite.couleur.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(tache.titre, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            Text(tache.rappel, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
        }
        Text("Dans ${heures}h ${minutes}m", color = VioletKrenoa, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    }
}
