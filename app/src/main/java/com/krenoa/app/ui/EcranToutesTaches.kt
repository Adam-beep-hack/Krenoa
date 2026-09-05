package com.krenoa.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.krenoa.app.data.chargerTaches
import com.krenoa.app.data.programmerAlarme
import com.krenoa.app.data.sauvegarderTaches
import com.krenoa.app.model.Tache
import java.util.Calendar

private val VioletKrenoa = Color(0xFF7B5CFF)
private val FondGris = Color(0xFFF5F4FA)

@Composable
fun EcranToutesTaches(onRetour: () -> Unit) {
    val context = LocalContext.current
    var taches by remember { mutableStateOf(chargerTaches(context)) }
    var tacheEnEdition by remember { mutableStateOf<Tache?>(null) }
    var afficherAjout by remember { mutableStateOf(false) }

    val debutJour = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val finJour = debutJour + 24 * 60 * 60 * 1000L

    val tachesDuJour = taches.filter { it.rappelMillis == 0L || it.rappelMillis in debutJour until finJour }
    val enCours = tachesDuJour.filter { !it.terminee }.sortedBy { it.rappelMillis }
    val terminees = tachesDuJour.filter { it.terminee }

    fun bascculer(tache: Tache) {
        taches = taches.map { if (it == tache) it.copy(terminee = !it.terminee) else it }
        sauvegarderTaches(context, taches)
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
                Text("Tâches du jour", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                if (enCours.isEmpty() && terminees.isEmpty()) {
                    item {
                        Text("Aucune tâche pour aujourd'hui", color = Color.Gray)
                    }
                }

                if (enCours.isNotEmpty()) {
                    item {
                        Text("À faire", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    items(enCours) { tache ->
                        CarteTacheComplete(tache = tache, surCoche = { bascculer(tache) }, onClick = { tacheEnEdition = tache })
                    }
                }

                if (terminees.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Terminées", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    items(terminees) { tache ->
                        CarteTacheComplete(tache = tache, surCoche = { bascculer(tache) }, onClick = { tacheEnEdition = tache })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { afficherAjout = true },
            containerColor = VioletKrenoa,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Ajouter une tâche", tint = Color.White)
        }
    }

    if (afficherAjout) {
        FormulaireTache(
            titre = "Nouvelle tâche",
            tacheExistante = null,
            onFermer = { afficherAjout = false },
            onValider = { titreV, rappelV, millisV, prioriteV, frequenceV, joursV ->
                val nouvelle = Tache(titreV, rappel = rappelV, rappelMillis = millisV, priorite = prioriteV, frequence = frequenceV, joursRepetition = joursV)
                val misesAJour = chargerTaches(context) + nouvelle
                sauvegarderTaches(context, misesAJour)
                taches = misesAJour
                programmerAlarme(context, nouvelle)
                afficherAjout = false
            }
        )
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
}

@Composable
private fun CarteTacheComplete(tache: Tache, surCoche: () -> Unit, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(checked = tache.terminee, onCheckedChange = { surCoche() }, colors = CheckboxDefaults.colors(checkedColor = VioletKrenoa))
                Column(modifier = Modifier.clickable { onClick() }) {
                    Text(
                        tache.titre,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (tache.terminee) TextDecoration.LineThrough else null
                    )
                    if (tache.rappel.isNotBlank()) {
                        Text(tache.rappel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
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
}
