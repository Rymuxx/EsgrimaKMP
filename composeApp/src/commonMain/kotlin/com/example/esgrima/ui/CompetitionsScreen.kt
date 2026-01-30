package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Arma
import com.example.esgrima.models.Competicion
import com.example.esgrima.models.Role
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionsScreen(onBack: () -> Unit, onSelectCompetition: (String) -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    val competitions by DataRepository.competitions.collectAsState()
    val currentUser by DataRepository.currentUser.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var organizer by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var selectedArma by remember { mutableStateOf(Arma.ESPADA) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Competiciones Disponibles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser?.role == Role.ADMIN) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Competición")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(competitions) { comp ->
                val isInscribed = currentUser?.linkedId != null && comp.inscritosIds.contains(currentUser?.linkedId)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        if (currentUser?.role != Role.FENCER || isInscribed) {
                            DataRepository.selectCompetition(comp.id)
                            onSelectCompetition(comp.id) 
                        }
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(comp.nombre, style = MaterialTheme.typography.titleLarge)
                                Text("${comp.arma} - ${comp.lugar}", style = MaterialTheme.typography.bodyMedium)
                                Text("Inscritos: ${comp.inscritosIds.size}", style = MaterialTheme.typography.labelSmall)
                            }

                            if (currentUser?.role == Role.FENCER) {
                                if (isInscribed) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Ya Inscrito") },
                                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                                    )
                                } else {
                                    Button(onClick = { DataRepository.inscribirseEnCompeticion(comp.id) }) {
                                        Text("Apuntarse")
                                    }
                                }
                            } else if (currentUser?.role == Role.ADMIN) {
                                Row {
                                    IconButton(onClick = { DataRepository.simulateResults(comp.id) }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Simular Resultados", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nueva Competición") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it }, 
                            label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = organizer, onValueChange = { organizer = it }, 
                            label = { Text("Organizador") }, modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = place, onValueChange = { place = it }, 
                            label = { Text("Lugar") }, modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text("Arma:", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Arma.entries.forEach { arma ->
                                FilterChip(
                                    selected = selectedArma == arma,
                                    onClick = { selectedArma = arma },
                                    label = { Text(arma.name) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            val newComp = Competicion(
                                id = Clock.System.now().toEpochMilliseconds().toString(),
                                nombre = name,
                                entidadOrganizadora = organizer,
                                fecha = "2025-01-20",
                                lugar = place,
                                arma = selectedArma
                            )
                            DataRepository.addCompetition(newComp)
                            showAddDialog = false
                            name = ""; organizer = ""; place = ""
                        }
                    }) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}
