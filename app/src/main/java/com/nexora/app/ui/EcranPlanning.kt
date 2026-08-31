package com.nexora.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexora.app.model.EvenementPlanning

@Composable
fun EcranPlanning() {

    var ouvrirAjout by remember {
        mutableStateOf(false)
    }

    val planning = remember {
        mutableStateListOf<EvenementPlanning>()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Mon Planning",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = { ouvrirAjout = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Ajouter")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            items(planning) { evenement ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            evenement.titre,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            "${evenement.jour} • ${evenement.heureDebut} - ${evenement.heureFin}"
                        )
                    }
                }
            }
        }
    }


    if (ouvrirAjout) {

        AjouterEvenementDialog(
            onFermer = { ouvrirAjout = false },

            onAjouter = {
                planning.add(it)
                ouvrirAjout = false
            }
        )
    }
}


@Composable
fun AjouterEvenementDialog(
    onFermer: () -> Unit,
    onAjouter: (EvenementPlanning) -> Unit
) {

    var titre by remember { mutableStateOf("") }
    var jour by remember { mutableStateOf("") }
    var debut by remember { mutableStateOf("") }
    var fin by remember { mutableStateOf("") }


    AlertDialog(

        onDismissRequest = onFermer,

        title = {
            Text("Nouvel événement")
        },

        text = {

            Column {

                OutlinedTextField(
                    value = titre,
                    onValueChange = { titre = it },
                    label = { Text("Titre") }
                )

                OutlinedTextField(
                    value = jour,
                    onValueChange = { jour = it },
                    label = { Text("Jour") }
                )

                OutlinedTextField(
                    value = debut,
                    onValueChange = { debut = it },
                    label = { Text("Début") }
                )

                OutlinedTextField(
                    value = fin,
                    onValueChange = { fin = it },
                    label = { Text("Fin") }
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    if (titre.isNotBlank()) {

                        onAjouter(
                            EvenementPlanning(
                                titre,
                                jour,
                                debut,
                                fin
                            )
                        )
                    }
                }
            ) {
                Text("Ajouter")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onFermer
            ) {
                Text("Annuler")
            }
        }
    )
}
