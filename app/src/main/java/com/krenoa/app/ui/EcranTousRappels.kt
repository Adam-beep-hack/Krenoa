package com.krenoa.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krenoa.app.data.chargerTaches
import com.krenoa.app.data.programmerAlarme
import com.krenoa.app.data.sauvegarderTaches
import com.krenoa.app.model.Tache

private val VioletKrenoa = Color(0xFF7B5CFF)
private val FondGris = Color(0xFFF5F4FA)

@Composable
fun EcranTousRappels(onRetour: () -> Unit) {
    val context = LocalContext.current
    var taches by remember { mutableStateOf(chargerTaches(context)) }
    var tacheEnEdition by remember { mutableStateOf<Tache?>(null) }

    val rappels = taches
        .filter { it.rappelMillis > System.currentTimeMillis() && !it.terminee }
        .sortedBy { it.rappelMillis }

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
                Text("Mes rappels", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                if (rappels.isEmpty()) {
                    item {
                        Text("Aucun rappel à venir", color = Color.Gray)
                    }
                } else {
                    items(rappels) { tache ->
                        CarteRappelComplete(tache = tache, onClick = { tacheEnEdition = tache })
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
}

@Composable
private fun CarteRappelComplete(tache: Tache, onClick: () -> Unit) {
    val diffMillis = tache.rappelMillis - System.currentTimeMillis()
    val heures = diffMillis / (1000 * 60 * 60)
    val minutes = (diffMillis / (1000 * 60)) % 60
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f).androidx.compose.foundation.clickable { onClick() }
            ) {
                Text(tache.titre, fontWeight = FontWeight.Medium)
                Text(tache.rappel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text("Dans ${heures}h ${minutes}m", color = VioletKrenoa, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}
