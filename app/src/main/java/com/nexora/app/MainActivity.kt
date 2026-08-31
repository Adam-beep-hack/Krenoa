package com.nexora.app

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Calendar

data class Tache(
    val titre: String,
    val terminee: Boolean = false,
    val rappel: String = "",
    val rappelMillis: Long = 0L
)

private val VioletNexora = Color(0xFF7B5CFF)
private val OrNexora = Color(0xFFF6B93B)
private val FondClair = Color(0xFFF9F8FF)

class MainActivity : ComponentActivity() {

    private val demandePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                demandePermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val schemaCouleurs = lightColorScheme(
                primary = VioletNexora,
                secondary = OrNexora,
                background = FondClair,
                surface = Color.White
            )
            MaterialTheme(colorScheme = schemaCouleurs) {
                Surface(modifier = Modifier.fillMaxSize(), color = FondClair) {
                    EcranPrincipal()
                }
            }
        }
    }
}

fun programmerAlarme(context: Context, tache: Tache) {
    if (tache.rappelMillis <= 0L) return
    val gestionnaireAlarme = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intention = Intent(context, RappelReceiver::class.java).apply {
        putExtra("titre", tache.titre)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        tache.titre.hashCode(),
        intention,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    gestionnaireAlarme.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        tache.rappelMillis,
        pendingIntent
    )
}

fun sauvegarderTaches(context: Context, taches: List<Tache>) {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texteASauvegarder = taches.joinToString(";;") { "${it.titre}~${if (it.terminee) 1 else 0}~${it.rappel}~${it.rappelMillis}" }
    prefs.edit().putString("taches", texteASauvegarder).apply()
}

fun chargerTaches(context: Context): List<Tache> {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texte = prefs.getString("taches", "") ?: ""
    if (texte.isBlank()) return emptyList()
    return texte.split(";;").mapNotNull { ligne ->
        val parties = ligne.split("~")
        when {
            parties.size >= 4 -> Tache(parties[0], parties[1] == "1", parties[2], parties[3].toLongOrNull() ?: 0L)
            parties.size == 3 -> Tache(parties[0], parties[1] == "1", parties[2])
            parties.size == 2 -> Tache(parties[0], parties[1] == "1")
            else -> null
        }
    }
}

@Composable
fun EcranPrincipal() {
    var ongletSelectionne by remember { mutableStateOf(0) }
    val onglets = listOf(
        Triple("Accueil", Icons.Filled.Home, 0),
        Triple("Tâches", Icons.Filled.CheckCircle, 1),
        Triple("Planning", Icons.Filled.DateRange, 2),
        Triple("Rappels", Icons.Filled.Notifications, 3),
        Triple("Profil", Icons.Filled.Person, 4)
    )

    Scaffold(
        containerColor = FondClair,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                onglets.forEach { (label, icone, index) ->
                    NavigationBarItem(
                        selected = ongletSelectionne == index,
                        onClick = { ongletSelectionne = index },
                        icon = { Icon(icone, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VioletNexora,
                            selectedTextColor = VioletNexora,
                            indicatorColor = VioletNexora.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingInterieur ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingInterieur)) {
            when (ongletSelectionne) {
                0 -> EcranPlaceholder("Accueil")
                1 -> EcranTaches()
                2 -> EcranPlaceholder("Planning")
                3 -> EcranPlaceholder("Rappels")
                4 -> EcranPlaceholder("Profil")
            }
        }
    }
}

@Composable
fun EcranPlaceholder(nom: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = nom, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Bientôt disponible", color = Color.Gray)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EcranTaches() {
    val context = LocalContext.current
    var taches by remember { mutableStateOf(chargerTaches(context)) }
    var texteNouvelleTache by remember { mutableStateOf("") }
    var tacheASupprimer by remember { mutableStateOf<Tache?>(null) }
    var rappelChoisiTexte by remember { mutableStateOf("") }
    var rappelChoisiMillis by remember { mutableStateOf(0L) }

    fun ouvrirSelecteurDateHeure() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, annee, mois, jour ->
                TimePickerDialog(
                    context,
                    { _, heure, minute ->
                        val calChoisi = Calendar.getInstance()
                        calChoisi.set(annee, mois, jour, heure, minute, 0)
                        rappelChoisiMillis = calChoisi.timeInMillis
                        rappelChoisiTexte = "%02d/%02d/%d à %02dh%02d".format(jour, mois + 1, annee, heure, minute)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(text = "NEXORA", style = MaterialTheme.typography.labelLarge, color = VioletNexora, fontWeight = FontWeight.Bold)
        Text(text = "Mes tâches", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = texteNouvelleTache,
            onValueChange = { texteNouvelleTache = it },
            label = { Text("Nouvelle tâche") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { ouvrirSelecteurDateHeure() }, shape = RoundedCornerShape(12.dp)) {
                Text(if (rappelChoisiTexte.isBlank()) "Choisir date/heure" else rappelChoisiTexte)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (texteNouvelleTache.isNotBlank()) {
                        val nouvelle = Tache(texteNouvelleTache, rappel = rappelChoisiTexte, rappelMillis = rappelChoisiMillis)
                        taches = taches + nouvelle
                        sauvegarderTaches(context, taches)
                        programmerAlarme(context, nouvelle)
                        texteNouvelleTache = ""
                        rappelChoisiTexte = ""
                        rappelChoisiMillis = 0L
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletNexora)
            ) {
                Text("Ajouter")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Astuce : appui long sur une tâche pour la supprimer", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(taches) { tache ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                            onLongClick = { tacheASupprimer = tache }
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tache.terminee,
                            onCheckedChange = { coche ->
                                taches = taches.map { if (it === tache) it.copy(terminee = coche) else it }
                                sauvegarderTaches(context, taches)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = VioletNexora)
                        )
                        Column {
                            Text(text = tache.titre, textDecoration = if (tache.terminee) TextDecoration.LineThrough else null)
                            if (tache.rappel.isNotBlank()) {
                                Text(text = tache.rappel, style = MaterialTheme.typography.bodySmall, color = VioletNexora)
                            }
                        }
                    }
                }
            }
        }
    }

    tacheASupprimer?.let { tache ->
        AlertDialog(
            onDismissRequest = { tacheASupprimer = null },
            title = { Text("Supprimer la tâche ?") },
            text = { Text("« ${tache.titre} » sera définitivement supprimée.") },
            confirmButton = {
                TextButton(onClick = {
                    taches = taches.filter { it !== tache }
                    sauvegarderTaches(context, taches)
                    tacheASupprimer = null
                }) { Text("Supprimer", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { tacheASupprimer = null }) { Text("Annuler") }
            }
        )
    }
}
