package com.nexora.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

data class Tache(val titre: String, val terminee: Boolean = false)

private val VioletNexora = Color(0xFF7B5CFF)
private val OrNexora = Color(0xFFF6B93B)
private val FondClair = Color(0xFFF9F8FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val schemaCouleurs = lightColorScheme(
                primary = VioletNexora,
                secondary = OrNexora,
                background = FondClair,
                surface = Color.White
            )
            MaterialTheme(colorScheme = schemaCouleurs) {
                Surface(modifier = Modifier.fillMaxSize(), color = FondClair) {
                    EcranTaches()
                }
            }
        }
    }
}

fun sauvegarderTaches(context: Context, taches: List<Tache>) {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texteASauvegarder = taches.joinToString(";;") { "${it.titre}|${if (it.terminee) 1 else 0}" }
    prefs.edit().putString("taches", texteASauvegarder).apply()
}

fun chargerTaches(context: Context): List<Tache> {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texte = prefs.getString("taches", "") ?: ""
    if (texte.isBlank()) return emptyList()
    return texte.split(";;").mapNotNull { ligne ->
        val parties = ligne.split("|")
        if (parties.size == 2) {
            Tache(titre = parties[0], terminee = parties[1] == "1")
        } else null
    }
}

@Composable
fun EcranTaches() {
    val context = LocalContext.current
    var taches by remember { mutableStateOf(chargerTaches(context)) }
    var texteNouvelleTache by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(
            text = "NEXORA",
            style = MaterialTheme.typography.labelLarge,
            color = VioletNexora,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Mes tâches",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = texteNouvelleTache,
                onValueChange = { texteNouvelleTache = it },
                label = { Text("Nouvelle tâche") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (texteNouvelleTache.isNotBlank()) {
                        taches = taches + Tache(texteNouvelleTache)
                        sauvegarderTaches(context, taches)
                        texteNouvelleTache = ""
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletNexora)
            ) {
                Text("Ajouter")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(taches) { tache ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tache.terminee,
                            onCheckedChange = { coche ->
                                taches = taches.map {
                                    if (it === tache) it.copy(terminee = coche) else it
                                }
                                sauvegarderTaches(context, taches)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = VioletNexora)
                        )
                        Text(
                            text = tache.titre,
                            textDecoration = if (tache.terminee) TextDecoration.LineThrough else null
                        )
                    }
                }
            }
        }
    }
}
