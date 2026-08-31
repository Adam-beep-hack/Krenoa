package com.nexora.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexora.app.model.BlocHoraire

@Composable
fun EcranPlanning() {

    var ouvrirAjout by remember {
        mutableStateOf(false)
    }

    val emploiDuTemps = remember {
        mutableStateListOf<BlocHoraire>()
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
                text = "Mon Emploi du Temps",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = {
                    ouvrirAjout = true
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Ajouter")
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            items(emploiDuTemps) { bloc ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = bloc.jour,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "${bloc.heureDebut} - ${bloc.heureFin}"
                        )

                        Text(
                            text = bloc.activite
                        )
                    }
                }
            }
        }
    }


    if (ouvrirAjout) {

        AjouterBlocHoraireDialog(
            onFermer = {
                ouvrirAjout = false
            },

            onAjouter = {
                emploiDuTemps.add(it)
                ouvrirAjout = false
            }
        )
    }
}



@Composable
fun AjouterBlocHoraireDialog(
    onFermer: () -> Unit,
    onAjouter: (BlocHoraire) -> Unit
) {

    var jour by remember { mutableStateOf("") }
    var debut by remember { mutableStateOf("") }
    var fin by remember { mutableStateOf("") }
    var activite by remember { mutableStateOf("") }


    AlertDialog(

        onDismissRequest = onFermer,

        title = {
            Text("Ajouter au planning")
        },

        text = {

            Column {

                OutlinedTextField(
                    value = jour,
                    onValueChange = { jour = it },
                    label = { Text("Jour") }
                )

                OutlinedTextField(
                    value = debut,
                    onValueChange = { debut = it },
                    label = { Text("Heure début") }
                )

                OutlinedTextField(
                    value = fin,
                    onValueChange = { fin = it },
                    label = { Text("Heure fin") }
                )

                OutlinedTextField(
                    value = activite,
                    onValueChange = { activite = it },
                    label = { Text("Activité") }
                )
            }
        },


        confirmButton = {

            TextButton(
                onClick = {

                    if (jour.isNotBlank() && activite.isNotBlank()) {

                        onAjouter(
                            BlocHoraire(
                                jour,
                                debut,
                                fin,
                                activite
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
