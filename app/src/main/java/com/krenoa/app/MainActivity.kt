package com.krenoa.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.krenoa.app.data.chargerTaches
import com.krenoa.app.data.programmerAlarme
import com.krenoa.app.data.sauvegarderTaches
import com.krenoa.app.model.Tache
import com.krenoa.app.ui.EcranAccueil
import com.krenoa.app.ui.EcranAssistant
import com.krenoa.app.ui.EcranPlanning
import com.krenoa.app.ui.EcranProfil
import com.krenoa.app.ui.EcranTaches
import com.krenoa.app.ui.EcranSplash
import com.krenoa.app.ui.EcranTousRappels
import com.krenoa.app.ui.EcranToutesTaches
import com.krenoa.app.ui.EcranToutesNotes
import com.krenoa.app.ui.EcranRecherche
import com.krenoa.app.ui.FormulaireTache

private val VioletKrenoa = Color(0xFF7B5CFF)
private val OrKrenoa = Color(0xFFF6B93B)
private val FondClair = Color(0xFFF9F8FF)

class MainActivity : ComponentActivity() {

    private val demandePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val tacheAOuvrir = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                demandePermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        tacheAOuvrir.value = intent.getStringExtra("ouvrir_tache")

        setContent {
            val schemaCouleurs = lightColorScheme(
                primary = VioletKrenoa,
                secondary = OrKrenoa,
                background = FondClair,
                surface = Color.White
            )
            MaterialTheme(colorScheme = schemaCouleurs) {
                Surface(modifier = Modifier.fillMaxSize(), color = FondClair) {
                    var affichageSplash by remember { mutableStateOf(true) }
                    if (affichageSplash) {
                        EcranSplash(surChargementTermine = { affichageSplash = false })
                    } else {
                        EcranPrincipal(
                            titreTacheAOuvrir = tacheAOuvrir.value,
                            onTacheOuverteConsommee = { tacheAOuvrir.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        tacheAOuvrir.value = intent.getStringExtra("ouvrir_tache")
    }
}

@Composable
fun EcranPrincipal(titreTacheAOuvrir: String? = null, onTacheOuverteConsommee: () -> Unit = {}) {
    var ongletSelectionne by remember { mutableStateOf(0) }
    var ecranDetail by remember { mutableStateOf<String?>(null) }
    var tacheDepuisNotif by remember { mutableStateOf<Tache?>(null) }
    val context = LocalContext.current

    LaunchedEffect(titreTacheAOuvrir) {
        if (titreTacheAOuvrir != null) {
            tacheDepuisNotif = chargerTaches(context).find { it.titre == titreTacheAOuvrir }
            onTacheOuverteConsommee()
        }
    }

    val onglets = listOf(
        Triple("Accueil", Icons.Filled.Home, 0),
        Triple("Tâches", Icons.Filled.CheckCircle, 1),
        Triple("Planning", Icons.Filled.DateRange, 2),
        Triple("Assistant", Icons.Filled.Chat, 3),
        Triple("Profil", Icons.Filled.Person, 4)
    )

    if (ecranDetail == "toutes_taches") {
        EcranToutesTaches(onRetour = { ecranDetail = null })
        return
    }

    if (ecranDetail == "tous_rappels") {
        EcranTousRappels(onRetour = { ecranDetail = null })
        return
    }

    if (ecranDetail == "toutes_notes") {
        EcranToutesNotes(onRetour = { ecranDetail = null })
        return
    }

    if (ecranDetail == "recherche") {
        EcranRecherche(onRetour = { ecranDetail = null })
        return
    }

    tacheDepuisNotif?.let { tache ->
        FormulaireTache(
            titre = "Modifier la tâche",
            tacheExistante = tache,
            onFermer = { tacheDepuisNotif = null },
            onValider = { titreV, rappelV, millisV, prioriteV, frequenceV, joursV ->
                val modifiee = tache.copy(titre = titreV, rappel = rappelV, rappelMillis = millisV, priorite = prioriteV, frequence = frequenceV, joursRepetition = joursV)
                val actuelles = chargerTaches(context)
                val misesAJour = actuelles.map { if (it == tache) modifiee else it }
                sauvegarderTaches(context, misesAJour)
                programmerAlarme(context, modifiee)
                tacheDepuisNotif = null
            }
        )
    }

    Scaffold(
        containerColor = FondClair,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                onglets.forEach { (label, icone, index) ->
                    NavigationBarItem(
                        selected = ongletSelectionne == index,
                        onClick = { ongletSelectionne = index },
                        icon = { Icon(icone, contentDescription = label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VioletKrenoa,
                            selectedTextColor = VioletKrenoa,
                            indicatorColor = VioletKrenoa.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingInterieur ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingInterieur)) {
            when (ongletSelectionne) {
                0 -> EcranAccueil(
                    chargerTaches(context),
                    onVoirToutTaches = { ecranDetail = "toutes_taches" },
                    onVoirToutRappels = { ecranDetail = "tous_rappels" },
                    onVoirToutNotes = { ecranDetail = "toutes_notes" },
                    onOuvrirRecherche = { ecranDetail = "recherche" }
                )
                1 -> EcranTaches()
                2 -> EcranPlanning()
                3 -> EcranAssistant()
                4 -> EcranProfil(chargerTaches(context))
            }
        }
    }
}
