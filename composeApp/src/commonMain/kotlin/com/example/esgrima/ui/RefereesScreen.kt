package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Arbitro
import com.example.esgrima.models.Arma
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefereesScreen(onBack: () -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    val referees by DataRepository.referees.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var selectedArmas by remember { mutableStateOf(setOf<Arma>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Árbitros") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Árbitro")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(referees) { referee ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(referee.nombre) },
                        supportingContent = { 
                            Text("Licencia: ${referee.numeroAfiliado} - Especialidades: ${referee.especialidades.joinToString { it.name }}") 
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nuevo Árbitro") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name, 
                            onValueChange = { name = it }, 
                            label = { Text("Nombre Completo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = license, 
                            onValueChange = { license = it }, 
                            label = { Text("Nº Licencia") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Especialidades:", style = MaterialTheme.typography.labelLarge)
                        Arma.entries.forEach { arma ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedArmas.contains(arma),
                                    onCheckedChange = { checked ->
                                        selectedArmas = if (checked) selectedArmas + arma else selectedArmas - arma
                                    }
                                )
                                Text(arma.name)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            val newReferee = Arbitro(
                                id = Clock.System.now().toEpochMilliseconds().toString(),
                                nombre = name,
                                numeroAfiliado = license,
                                especialidades = selectedArmas.toList()
                            )
                            DataRepository.addReferee(newReferee)
                            showAddDialog = false
                            name = ""; license = ""; selectedArmas = emptySet()
                        }
                    }) {
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
