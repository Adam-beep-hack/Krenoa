package com.nexora.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

data class Tache(val titre: String, val terminee: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Mes tâches", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = texteNouvelleTache,
                onValueChange = { texteNouvelleTache = it },
                label = { Text("Nouvelle tâche") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (texteNouvelleTache.isNotBlank()) {
                    taches = taches + Tache(texteNouvelleTache)
                    sauvegarderTaches(context, taches)
                    texteNouvelleTache = ""
                }
            }) {
                Text("Ajouter")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(taches) { tache ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tache.terminee,
                        onCheckedChange = { coche ->
                            taches = taches.map {
                                if (it === tache) it.copy(terminee = coche) else it
                            }
                            sauvegarderTaches(context, taches)
                        }
                    )
                    Text(
                        text = tache.titre,
                        textDecoration = if (tache.terminee) TextDecoration.LineThrough else null
                    )
                }
            }
        }
    }
}                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tache.terminee,
                        onCheckedChange = { coche ->
                            taches = taches.map {
                                if (it === tache) it.copy(terminee = coche) else it
                            }
                        }
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
