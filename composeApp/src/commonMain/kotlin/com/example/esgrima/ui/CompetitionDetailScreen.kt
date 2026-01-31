package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Competicion
import com.example.esgrima.models.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionDetailScreen(competitionId: String, onBack: () -> Unit) {
    val competitions by DataRepository.competitions.collectAsState()
    val comp = competitions.find { it.id == competitionId }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Poules", "Eliminatorias", "Inscritos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(comp?.nombre ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (comp == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("Competición no encontrada")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                when (selectedTab) {
                    0 -> PouleTabContent(comp)
                    1 -> EliminatoriasTabContent(comp)
                    2 -> InscritosTabContent(comp)
                }
            }
        }
    }
}

@Composable
fun InscritosTabContent(comp: Competicion) {
    val fencers by DataRepository.fencers.collectAsState()
    val currentUser by DataRepository.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (currentUser?.role == Role.ADMIN) {
            Button(
                onClick = { DataRepository.inscribirATodos(comp.id) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Inscribir a todos los Tiradores")
            }
        }

        Text("Lista de Tiradores", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(fencers) { fencer ->
                val isInscribed = comp.inscritosIds.contains(fencer.id)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { 
                        if (currentUser?.role == Role.ADMIN) {
                            DataRepository.inscribirseEnCompeticionConId(comp.id, fencer.id)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(fencer.nombre, style = MaterialTheme.typography.bodyLarge)
                            Text(fencer.club.nombre, style = MaterialTheme.typography.labelMedium)
                        }
                        if (isInscribed) {
                            Icon(Icons.Default.Check, contentDescription = "Inscrito", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PouleTabContent(comp: Competicion) {
    var showPouleDialog by remember { mutableStateOf(false) }
    var numPoules by remember { mutableStateOf("1") }
    val currentUser by DataRepository.currentUser.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (comp.inscritosIds.size < 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Se necesitan al menos 2 tiradores inscritos.")
            }
        } else {
            if (currentUser?.role == Role.ADMIN) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { DataRepository.simulateResults(comp.id) }) {
                        Text("Simular Resultados")
                    }
                    Button(onClick = { showPouleDialog = true }) {
                        Text(if (comp.poules.isEmpty()) "Generar Poules" else "Re-generar Poules")
                    }
                }
            }

            if (comp.poules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay poules generadas.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(comp.poules.size) { index ->
                        val poule = comp.poules[index]
                        Text(poule.nombre, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        poule.asaltos.forEach { asalto ->
                            AsaltoItem(comp.id, poule.id, asalto)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 2.dp)
                    }
                }
            }
        }
    }

    if (showPouleDialog) {
        AlertDialog(
            onDismissRequest = { showPouleDialog = false },
            title = { Text("Configurar Poules") },
            text = {
                Column {
                    Text("¿Cuántas poules quieres generar?")
                    OutlinedTextField(
                        value = numPoules,
                        onValueChange = { if (it.all { char -> char.isDigit() }) numPoules = it },
                        label = { Text("Número de Poules") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val n = numPoules.toIntOrNull() ?: 1
                    DataRepository.createPoulesAutomaticamente(comp.id, n)
                    showPouleDialog = false
                }) {
                    Text("Generar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPouleDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun EliminatoriasTabContent(comp: Competicion) {
    val currentUser by DataRepository.currentUser.collectAsState()

    if (comp.rondasEliminatorias.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("El cuadro aún no ha sido generado.")
                if (currentUser?.role == Role.ADMIN) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { DataRepository.generarCuadroEliminatorio(comp.id) }) {
                        Text("Generar Cuadro desde Ranking")
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val rondas = comp.rondasEliminatorias.reversed()
            items(rondas.size) { index ->
                val ronda = rondas[index]
                Text(ronda.nombre, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                ronda.asaltos.forEach { asalto ->
                    AsaltoEliminatorioItem(comp.id, ronda.nombre, asalto)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            val ultimaRonda = comp.rondasEliminatorias.last()
            val todosTerminados = ultimaRonda.asaltos.all { it.terminado }
            val noEsFinal = ultimaRonda.asaltos.size > 1

            if (todosTerminados && noEsFinal && currentUser?.role == Role.ADMIN) {
               item {
                   Button(
                       onClick = { DataRepository.avanzarCuadro(comp.id) },
                       modifier = Modifier.fillMaxWidth()
                   ) {
                       Text("Avanzar Siguiente Ronda")
                   }
               }
            }
        }
    }
}
