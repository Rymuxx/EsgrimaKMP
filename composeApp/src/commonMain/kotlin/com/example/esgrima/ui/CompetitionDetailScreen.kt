package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Competicion
import com.example.esgrima.models.Role
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionDetailScreen(competitionId: String, onBack: () -> Unit) {
    val competitions by DataRepository.competitions.collectAsState()
    val comp = competitions.find { it.id == competitionId }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Poules", "Clasificación", "Eliminatorias", "Inscritos")

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
                    1 -> ClasificacionTabContent(comp)
                    2 -> EliminatoriasScreen(onBack = {})
                    3 -> InscritosTabContent(comp)
                }
            }
        }
    }
}

// Función auxiliar para formatear decimales en commonMain
private fun Double.format(decimals: Int): String {
    val factor = 10.0.let { base -> 
        var res = 1.0
        repeat(decimals) { res *= base }
        res
    }
    val rounded = (this * factor).roundToInt() / factor
    val s = rounded.toString()
    return if (s.contains(".")) {
        val parts = s.split(".")
        val decimalPart = parts[1].padEnd(decimals, '0').take(decimals)
        "${parts[0]}.$decimalPart"
    } else {
        s + "." + "0".repeat(decimals)
    }
}

@Composable
fun ClasificacionTabContent(comp: Competicion) {
    val ranking = DataRepository.getRankingCalculado(comp)
    val corte = comp.numClasificadosCorte

    if (ranking.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay resultados suficientes para generar la clasificación.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Pos", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                    Text("Tirador", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Estado", modifier = Modifier.width(80.dp), fontWeight = FontWeight.Bold)
                    Text("V/M", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold)
                    Text("Ind.", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold)
                }
                HorizontalDivider()
            }
            itemsIndexed(ranking) { index, item ->
                val estaEnCorte = index < corte
                val bgColor = if (estaEnCorte) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                
                Surface(color = bgColor, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${item.posicion}", modifier = Modifier.width(40.dp))
                        Text(item.tirador.nombre, modifier = Modifier.weight(1f))
                        
                        Text(
                            text = if (estaEnCorte) "DENTRO" else "ELIMIN.",
                            modifier = Modifier.width(80.dp),
                            color = if (estaEnCorte) Color(0xFF2E7D32) else Color(0xFFC62828),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(item.v_m.format(2), modifier = Modifier.width(50.dp))
                        Text("${if (item.indice > 0) "+" else ""}${item.indice}", modifier = Modifier.width(50.dp))
                    }
                }
                HorizontalDivider()
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
                        PouleMatrix(comp, poule)
                        Spacer(modifier = Modifier.height(16.dp))
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
