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
    PERSONNALISE("Jours personnalisés")
}

data class JourSemaine(val libelle: String, val valeurCalendar: Int)

val JOURS_SEMAINE = listOf(
    JourSemaine("Lun", Calendar.MONDAY),
    JourSemaine("Mar", Calendar.TUESDAY),
    JourSemaine("Mer", Calendar.WEDNESDAY),
    JourSemaine("Jeu", Calendar.THURSDAY),
    JourSemaine("Ven", Calendar.FRIDAY),
    JourSemaine("Sam", Calendar.SATURDAY),
    JourSemaine("Dim", Calendar.SUNDAY)
)

data class Tache(
    val titre: String,
    val terminee: Boolean = false,
    val rappel: String = "",
    val rappelMillis: Long = 0L,
    val priorite: Priorite = Priorite.MOYENNE,
    val frequence: Frequence = Frequence.UNIQUE,
    val joursSelectionnes: Set<Int> = emptySet()
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

fun prochainJour(heure: Int, minute: Int, jourCalendar: Int): Calendar {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, heure)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    val maintenant = Calendar.getInstance()
    while (cal.get(Calendar.DAY_OF_WEEK) != jourCalendar || cal.before(maintenant)) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return cal
}

fun programmerAlarme(context: Context, tache: Tache) {
    val gestionnaireAlarme = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (tache.frequence == Frequence.PERSONNALISE) {
        if (tache.joursSelectionnes.isEmpty() || tache.rappelMillis <= 0L) return
        val calRef = Calendar.getInstance().apply { timeInMillis = tache.rappelMillis }
        val heure = calRef.get(Calendar.HOUR_OF_DAY)
        val minute = calRef.get(Calendar.MINUTE)
        tache.joursSelectionnes.forEach { jour ->
            val calCible = prochainJour(heure, minute, jour)
            val intention = Intent(context, RappelReceiver::class.java).apply {
                putExtra("titre", tache.titre)
                putExtra("frequence", tache.frequence.name)
                putExtra("jourDeclenche", jour)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                tache.titre.hashCode() + jour,
                intention,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            gestionnaireAlarme.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calCible.timeInMillis, pendingIntent)
        }
    } else {
        if (tache.rappelMillis <= 0L) return
        val intention = Intent(context, RappelReceiver::class.java).apply {
            putExtra("titre", tache.titre)
            putExtra("frequence", tache.frequence.name)
            putExtra("jourDeclenche", -1)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tache.titre.hashCode(),
            intention,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        gestionnaireAlarme.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tache.rappelMillis, pendingIntent)
    }
}

fun sauvegarderTaches(context: Context, taches: List<Tache>) {
    val prefs = context.getSharedPreferences("nexora_prefs", Context.MODE_PRIVATE)
    val texteASauvegarder = taches.joinToString(";;") {
        "${it.titre}~${if (it.terminee) 1 else 0}~${it.rappel}~${it.rappelMillis}~${it.priorite.name}~${it.frequence.name}~${it.joursSelectionnes.joinToString(",")}"
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
                2 -> EcranPlanning(taches = chargerTaches(LocalContext.current))
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

@Composable
fun EcranPlanning(taches: List<Tache>) {
    var joursDecalage by remember { mutableStateOf(0) }
    val calAffiche = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, joursDecalage) }
    val jourSemaineAffiche = calAffiche.get(Calendar.DAY_OF_WEEK)
    val libelleJour = JOURS_SEMAINE.find { it.valeurCalendar == jourSemaineAffiche }?.libelle ?: ""
    val dateTexte = "%s %02d/%02d/%d".format(libelleJour, calAffiche.get(Calendar.DAY_OF_MONTH), calAffiche.get(Calendar.MONTH) + 1, calAffiche.get(Calendar.YEAR))

    val tachesDuJour = taches.filter { tache ->
        if (tache.frequence == Frequence.PERSONNALISE) {
            tache.joursSelectionnes.contains(jourSemaineAffiche)
        } else if (tache.frequence == Frequence.QUOTIDIEN) {
            true
        } else if (tache.rappelMillis > 0L) {
            val calTache = Calendar.getInstance().apply { timeInMillis = tache.rappelMillis }
            calTache.get(Calendar.DAY_OF_YEAR) == calAffiche.get(Calendar.DAY_OF_YEAR) &&
                calTache.get(Calendar.YEAR) == calAffiche.get(Calendar.YEAR)
        } else false
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text(text = "Planning", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { joursDecalage -= 1 }) { Text("◀ Précédent") }
            Text(dateTexte, fontWeight = FontWeight.Bold)
            TextButton(onClick = { joursDecalage += 1 }) { Text("Suivant ▶") }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (tachesDuJour.isEmpty()) {
            Text("Aucune tâche ce jour-là", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tachesDuJour) { tache ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.padding(end = 8.dp).size(10.dp)) {
                                Surface(color = tache.priorite.couleur, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxSize()) {}
                            }
                            Column {
                                Text(tache.titre, fontWeight = FontWeight.Medium)
                                if (tache.rappel.isNotBlank()) {
                                    Text(tache.rappel, style = MaterialTheme.typography.bodySmall, color = VioletNexora)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormulaireTache(
    titre: String,
    onFermer: () -> Unit,
    onValider: (String, String, Long, Priorite, Frequence, Set<Int>) -> Unit,
    tacheExistante: Tache? = null
) {
    val context = LocalContext.current
    var texteTitre by remember { mutableStateOf(tacheExistante?.titre ?: "") }
    var rappelTexte by remember { mutableStateOf(tacheExistante?.rappel ?: "") }
    var rappelMillis by remember { mutableStateOf(tacheExistante?.rappelMillis ?: 0L) }
    var priorite by remember { mutableStateOf(tacheExistante?.priorite ?: Priorite.MOYENNE) }
    var frequence by remember { mutableStateOf(tacheExistante?.frequence ?: Frequence.UNIQUE) }
    var joursSelectionnes by remember { mutableStateOf(tacheExistante?.joursSelectionnes ?: emptySet()) }
    var menuPrioriteOuvert by remember { mutableStateOf(false) }
    var menuFrequenceOuvert by remember { mutableStateOf(false) }

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
                        rappelMillis = calChoisi.timeInMillis
                        rappelTexte = "%02d/%02d/%d à %02dh%02d".format(jour, mois + 1, annee, heure, minute)
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

    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text(titre) },
        text = {
            Column {
                OutlinedTextField(
                    value = texteTitre,
                    onValueChange = { texteTitre = it },
                    label = { Text("Titre") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { ouvrirSelecteurDateHeure() }, shape = RoundedCornerShape(12.dp)) {
                    Text(if (rappelTexte.isBlank()) "Date/heure" else rappelTexte)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(
                        onClick = { menuPrioriteOuvert = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = priorite.couleur)
                    ) {
                        Text(priorite.libelle)
                    }
                    DropdownMenu(expanded = menuPrioriteOuvert, onDismissRequest = { menuPrioriteOuvert = false }) {
                        Priorite.values().forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.libelle, color = p.couleur) },
                                onClick = { priorite = p; menuPrioriteOuvert = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { menuFrequenceOuvert = true }, shape = RoundedCornerShape(12.dp)) {
                        Text(frequence.libelle)
                    }
                    DropdownMenu(expanded = menuFrequenceOuvert, onDismissRequest = { menuFrequenceOuvert = false }) {
                        Frequence.values().forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f.libelle) },
                                onClick = { frequence = f; menuFrequenceOuvert = false }
                            )
                        }
                    }
                }
                if (frequence == Frequence.PERSONNALISE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Choisir les jours :", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        JOURS_SEMAINE.forEach { jourSemaine ->
                            val selectionne = joursSelectionnes.contains(jourSemaine.valeurCalendar)
                            FilterChip(
                                selected = selectionne,
                                onClick = {
                                    joursSelectionnes = if (selectionne) {
                                        joursSelectionnes - jourSemaine.valeurCalendar
                                    } else {
                                        joursSelectionnes + jourSemaine.valeurCalendar
                                    }
                                },
                                label = { Text(jourSemaine.libelle) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VioletNexora.copy(alpha = 0.2f))
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (texteTitre.isNotBlank()) {
                    onValider(texteTitre, rappelTexte, rappelMillis, priorite, frequence, joursSelectionnes)
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onFermer) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EcranTaches() {
    val context = LocalContext.current
    var taches by remember { mutableStateOf(chargerTaches(context)) }
    var tacheASupprimer by remember { mutableStateOf<Tache?>(null) }
    var tacheAModifier by remember { mutableStateOf<Tache?>(null) }
    var ajoutOuvert by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "NEXORA", style = MaterialTheme.typography.labelLarge, color = VioletNexora, fontWeight = FontWeight.Bold)
                Text(text = "Mes tâches", style = 
