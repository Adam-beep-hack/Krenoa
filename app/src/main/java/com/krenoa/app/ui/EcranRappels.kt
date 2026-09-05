package com.krenoa.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krenoa.app.model.Frequence
import com.krenoa.app.model.Tache

private val VioletKrenoa = Color(0xFF7B5CFF)

@Composable
fun EcranRappels(taches: List<Tache>) {
    val tachesAvecRappel = taches.filter { it.rappelMillis > 0 }.sortedBy { it.rappelMillis }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(text = "Mes rappels", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (tachesAvecRappel.isEmpty()) {
            Text(text = "Aucun rappel programmé", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tachesAvecRappel) { tache ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.padding(end = 8.dp).size(10.dp)) {
                                Surface(color = tache.priorite.couleur, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxSize()) {}
                            }
                            Column {
                                Text(text = tache.titre, fontWeight = FontWeight.Bold)
                                val details = listOfNotNull(
                                    tache.rappel.takeIf { it.isNotBlank() },
                                    tache.frequence.libelle.takeIf { tache.frequence != Frequence.UNIQUE }
                                ).joinToString(" · ")
                                Text(text = details, style = MaterialTheme.typography.bodySmall, color = VioletKrenoa)
                            }
                        }
                    }
                }
            }
        }
    }
}
