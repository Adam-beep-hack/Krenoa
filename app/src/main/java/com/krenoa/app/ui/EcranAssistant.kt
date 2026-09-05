package com.krenoa.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.krenoa.app.data.demanderAGemini
import kotlinx.coroutines.launch

private val VioletKrenoa = Color(0xFF7B5CFF)

data class MessageChat(val texte: String, val estUtilisateur: Boolean)

@Composable
fun EcranAssistant() {
    var messages by remember { mutableStateOf(listOf<MessageChat>()) }
    var texteSaisi by remember { mutableStateOf("") }
    var enCours by remember { mutableStateOf(false) }
    val portee = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(text = "Assistant Krenoa", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.estUtilisateur) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.estUtilisateur) VioletKrenoa else Color(0xFFEDEBFF)
                        )
                    ) {
                        Text(
                            text = message.texte,
                            color = if (message.estUtilisateur) Color.White else Color.Black,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
            if (enCours) {
                item {
                    Text(text = "L'assistant réfléchit...", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = texteSaisi,
                onValueChange = { texteSaisi = it },
                label = { Text("Pose une question") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (texteSaisi.isNotBlank() && !enCours) {
                        val question = texteSaisi
                        messages = messages + MessageChat(question, true)
                        texteSaisi = ""
                        enCours = true
                        portee.launch {
                            val reponse = demanderAGemini(question)
                            messages = messages + MessageChat(reponse, false)
                            enCours = false
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletKrenoa)
            ) {
                Text("Envoyer")
            }
        }
    }
}
