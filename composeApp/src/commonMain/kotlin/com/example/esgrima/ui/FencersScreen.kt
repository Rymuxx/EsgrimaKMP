package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Arma
import com.example.esgrima.models.Club
import com.example.esgrima.models.Tirador
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiradoresScreen(onBack: () -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    val tiradores by DataRepository.fencers.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var clubName by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var selectedArmas by remember { mutableStateOf(setOf<Arma>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Tiradores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Tirador")
            }
        }
    ) { padding ->
        // Usamos LazyVerticalGrid para que sea responsive en Desktop
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tiradores) { fencer ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(fencer.nombre) },
                        supportingContent = { 
                            Column {
                                Text("${fencer.club.nombre} - Licencia: ${fencer.numeroAfiliado}")
                                Text(
                                    text = "Especialidades: ${fencer.especialidades.joinToString { it.name }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { DataRepository.deleteFencer(fencer.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nuevo Tirador") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = clubName, onValueChange = { clubName = it }, label = { Text("Club") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("Nº Licencia") }, modifier = Modifier.fillMaxWidth())
                        
                        Text("Armas que practica:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Arma.entries.forEach { arma ->
                                FilterChip(
                                    selected = selectedArmas.contains(arma),
                                    onClick = {
                                        selectedArmas = if (selectedArmas.contains(arma)) {
                                            selectedArmas - arma
                                        } else {
                                            selectedArmas + arma
                                        }
                                    },
                                    label = { Text(arma.name) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank() && selectedArmas.isNotEmpty()) {
                                DataRepository.addFencer(Tirador(
                                    id = Clock.System.now().toEpochMilliseconds().toString(),
                                    nombre = name,
                                    club = Club(clubName, ""),
                                    numeroAfiliado = license,
                                    especialidades = selectedArmas.toList()
                                ))
                                showAddDialog = false
                                name = ""; clubName = ""; license = ""; selectedArmas = emptySet()
                            }
                        },
                        enabled = name.isNotBlank() && selectedArmas.isNotEmpty()
                    ) {
                        Text("Añadir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}
