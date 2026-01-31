package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tiradores) { fencer ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(fencer.nombre) },
                        supportingContent = { Text("${fencer.club.nombre} - Licencia: ${fencer.numeroAfiliado}") }
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
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            DataRepository.addFencer(Tirador(
                                id = Clock.System.now().toEpochMilliseconds().toString(),
                                nombre = name,
                                club = Club(clubName, ""),
                                numeroAfiliado = license
                            ))
                            showAddDialog = false
                            name = ""; clubName = ""; license = ""
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
