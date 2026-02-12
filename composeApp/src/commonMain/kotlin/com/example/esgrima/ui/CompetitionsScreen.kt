package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
    var numCorte by remember { mutableStateOf("16") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Competiciones") },
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
                        // Permitimos entrar a ver si eres Admin, Árbitro o Tirador Inscrito
                        if (currentUser?.role != Role.FENCER || isInscribed) {
                            DataRepository.selectCompetition(comp.id)
                            onSelectCompetition(comp.id) 
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(comp.nombre, style = MaterialTheme.typography.titleLarge)
                            Text("${comp.arma} - ${comp.lugar}", style = MaterialTheme.typography.bodyMedium)
                            Text("Corte: ${comp.numClasificadosCorte} | Inscritos: ${comp.inscritosIds.size}", style = MaterialTheme.typography.labelSmall)
                        }

                        if (currentUser?.role == Role.ADMIN) {
                            IconButton(onClick = { DataRepository.deleteCompetition(comp.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        if (currentUser?.role == Role.FENCER) {
                            if (isInscribed) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Inscrito", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                Button(onClick = { DataRepository.inscribirseEnCompeticion(comp.id) }) {
                                    Text("Inscribirse")
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
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                        OutlinedTextField(value = organizer, onValueChange = { organizer = it }, label = { Text("Organizador") })
                        OutlinedTextField(value = place, onValueChange = { place = it }, label = { Text("Lugar") })
                        OutlinedTextField(
                            value = numCorte, 
                            onValueChange = { if (it.all { char -> char.isDigit() }) numCorte = it }, 
                            label = { Text("Nº Clasificados (Corte)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Arma:")
                        Row {
                            Arma.entries.forEach { arma ->
                                FilterChip(
                                    selected = selectedArma == arma,
                                    onClick = { selectedArma = arma },
                                    label = { Text(arma.name) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val newComp = Competicion(
                            id = Clock.System.now().toEpochMilliseconds().toString(),
                            nombre = name,
                            entidadOrganizadora = organizer,
                            fecha = "2025-01-20",
                            lugar = place,
                            arma = selectedArma,
                            numClasificadosCorte = numCorte.toIntOrNull() ?: 16
                        )
                        DataRepository.addCompetition(newComp)
                        showAddDialog = false
                    }) { Text("Crear") }
                }
            )
        }
    }
}
