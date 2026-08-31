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

enum class Priorite(val libelle: String, val couleur: Color) {
    URGENTE("Urgente", Color(0xFFE53935)),
    ELEVEE("Élevée", Color(0xFFFB8C00)),
    MOYENNE("Moyenne", Color(0xFFFDD835)),
    FAIBLE("Faible", Color(0xFF43A047))
}

enum class Frequence(val libelle: String) {
    UNIQUE("Ne pas répéter"),
    QUOTIDIEN("Tous les jours"),
    HEBDOMADAIRE("Toutes les semaines"),
    PERSONNALISE("Jours précis")
}

val NomsJours = listOf(
    Calendar.SUNDAY to "Dim",
    Calendar.MONDAY to "Lun",
    Calendar.TUESDAY to "Mar",
    Calendar.WEDNESDAY to "Mer",
    Calendar.THURSDAY to "Jeu",
    Calendar.FRIDAY to "Ven",
    Calendar.SATURDAY to "Sam"
)

data class Tache(
    val titre: String,
    val terminee: Boolean = false,
    val rappel: String = "",
    val rappelMillis: Long = 0L,
    val priorite: Priorite = Priorite.MOYENNE,
    val frequence: Frequence = Frequence.UNIQUE,
    val joursRepetition: Set<Int> = emptySet()
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

    var millisCible = tache.rappelMillis

    if (tache.frequence == Frequence.PERSONNALISE && tache.joursRepetition.isNotEmpty()) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = tache.rappelMillis
        if (!tache.joursRepetition.contains(cal.get(Calendar.DAY_OF_WEEK))) {
            for (i in 1..7) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                if (tache.joursRepetition.contains(cal.get(Calendar.DAY_OF_WEEK))) break
            }
            millisCible = cal.timeInMillis
        }
    }

    val intention = Intent(context, RappelReceiver::class.java).apply {
        putExtra("titre", tache.titre)
        putExtra("frequence", tache.frequence.name)
        putExtra("jours", tache.joursRepetition.joinToString(","))
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        tache.titre.hashCode(),
        intention,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    gestionnaireAlarme.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        millisCible,
        pendingIntent
    )
}

fun sauvegarderTaches(context: Context, taches: List<Tache>) {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texteASauvegarder = taches.joinToString(";;") {
        "${it.titre}~${if (it.terminee) 1 else 0}~${it.rappel}~${it.rappelMillis}~${it.priorite.name}~${it.frequence.name}~${it.joursRepetition.joinToString(",")}"
    }
    prefs.edit().putString("taches", texteASauvegarder).apply()
}

fun chargerTaches(context: Context): List<Tache> {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texte = prefs.getString("taches", "") ?: ""
    if (texte.isBlank()) return emptyList()
    return texte.split(";;").mapNotNull { ligne ->
        val parties = ligne.split("~")
        val priorite = if (parties.size >= 5) {
            try { Priorite.valueOf(parties[4]) } catch (e: Exception) { Priorite.MOYENNE }
        } else Priorite.MOYENNE
        val frequence = if (parties.size >= 6) {
            try { Frequence.valueOf(parties[5]) } catch (e: Exception) { Frequence.UNIQUE }
        } else Frequence.UNIQUE
        val jours = if (parties.size >= 7 && parties[6].isNotBlank()) {
            parties[6].split(",").mapNotNull { it.toIntOrNull() }.toSet()
        } else emptySet()
        when {
            parties.size >= 4 -> Tache(parties[0], parties[1] == "1", parties[2], parties[3].toLongOrNull() ?: 0L, priorite, frequence, jours)
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
