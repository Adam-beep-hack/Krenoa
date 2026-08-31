package com.nexora.app

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

@Composable
fun EcranTaches() {
    var taches by remember { mutableStateOf(listOf<Tache>()) }
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
