package com.nexora.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.nexora.app.data.chargerTaches
import com.nexora.app.ui.EcranAccueil
import com.nexora.app.ui.EcranPlanning
import com.nexora.app.ui.EcranRappels
import com.nexora.app.ui.EcranTaches

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

@Composable
fun EcranPrincipal() {
    var ongletSelectionne by remember { mutableStateOf(0) }
    val context = LocalContext.current
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
                0 -> EcranAccueil(chargerTaches(context))
                1 -> EcranTaches()
                2 -> EcranPlanning()
                3 -> EcranRappels(chargerTaches(context))
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
