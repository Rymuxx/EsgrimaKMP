package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Role

data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigate: (String) -> Unit) {
    val currentUser by DataRepository.currentUser.collectAsState()
    var showRoleDialog by remember { mutableStateOf(false) }
    
    val allItems = listOf(
        DashboardItem("Competiciones", Icons.Default.EmojiEvents, "competitions"),
        DashboardItem("Ranking Global", Icons.Default.FormatListNumbered, "ranking"),
        DashboardItem("Tiradores", Icons.Default.Person, "fencers"),
        DashboardItem("Árbitros", Icons.Default.Gavel, "referees")
    )

    val visibleItems = when (currentUser?.role) {
        Role.ADMIN -> allItems
        Role.REFEREE -> allItems.filter { it.route in listOf("competitions", "ranking") }
        Role.FENCER -> allItems.filter { it.route in listOf("competitions", "ranking") }
        null -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Esgrima Pro")
                        Text(
                            text = "Rol actual: ${currentUser?.role}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showRoleDialog = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Cambiar Rol")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(visibleItems) { item ->
                    Card(
                        onClick = { onNavigate(item.route) },
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

        if (showRoleDialog) {
            AlertDialog(
                onDismissRequest = { showRoleDialog = false },
                title = { Text("Seleccionar Rol") },
                text = {
                    Column {
                        Role.entries.forEach { role ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentUser?.role == role,
                                    onClick = {
                                        DataRepository.switchRole(role)
                                        showRoleDialog = false
                                    }
                                )
                                Text(role.name, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRoleDialog = false }) { Text("Cerrar") }
                }
            )
        }
    }
}
