package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Role

data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val action: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    val currentUser by DataRepository.currentUser.collectAsState()
    
    val allItems = listOf(
        DashboardItem("Competiciones", Icons.Default.EmojiEvents, "competitions"),
        DashboardItem("Tiradores", Icons.Default.Person, "fencers"),
        DashboardItem("Árbitros", Icons.Default.Gavel, "referees"),
        DashboardItem("Poules", Icons.Default.Groups, "poules"),
        DashboardItem("Resultados", Icons.Default.Scoreboard, "results"),
        DashboardItem("Clasificación", Icons.Default.FormatListNumbered, "ranking"),
        DashboardItem("Guardar", Icons.Default.Save, "save") { DataRepository.guardarEnDisco() },
        DashboardItem("Cargar", Icons.Default.FileUpload, "load") { DataRepository.cargarDesdeDisco() },
        DashboardItem("Cerrar Sesión", Icons.Default.Logout, "logout")
    )

    val visibleItems = when (currentUser?.role) {
        Role.ADMIN -> allItems
        Role.REFEREE -> allItems.filter { it.route in listOf("competitions", "poules", "results", "ranking", "logout") }
        Role.FENCER -> allItems.filter { it.route in listOf("competitions", "ranking", "logout") }
        null -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Esgrima Pro - PC")
                        Text(
                            text = "Conectado como: ${currentUser?.username} (${currentUser?.role})",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = padding,
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(visibleItems) { item ->
                Card(
                    onClick = { 
                        if (item.action != null) item.action.invoke()
                        else onNavigate(item.route) 
                    },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
