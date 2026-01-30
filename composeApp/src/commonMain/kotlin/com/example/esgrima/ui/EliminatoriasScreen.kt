package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Asalto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EliminatoriasScreen(onBack: () -> Unit) {
    val selectedCompId by DataRepository.selectedCompetitionId.collectAsState()
    val competitions by DataRepository.competitions.collectAsState()
    val comp = competitions.find { it.id == selectedCompId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(comp?.let { "Eliminatorias - ${it.nombre}" } ?: "Cuadro Eliminatorio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (comp != null && comp.rondasEliminatorias.isNotEmpty() && comp.rondasEliminatorias.last().asaltos.all { it.terminado }) {
                        IconButton(onClick = { DataRepository.avanzarCuadro(comp.id) }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Siguiente Ronda")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (comp == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Selecciona una competición primero.")
            }
        } else if (comp.rondasEliminatorias.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("El cuadro aún no ha sido generado.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { DataRepository.generarCuadroEliminatorio(comp.id) }) {
                        Text("Generar Cuadro desde Ranking")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                comp.rondasEliminatorias.reversed().forEach { ronda ->
                    item {
                        Text(ronda.nombre, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    items(ronda.asaltos) { asalto ->
                        AsaltoEliminatorioItem(comp.id, ronda.nombre, asalto)
                    }
                }
            }
        }
    }
}

@Composable
fun AsaltoEliminatorioItem(competitionId: String, nombreRonda: String, asalto: Asalto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Árbitro: ${asalto.arbitro.nombre}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tirador 1", style = MaterialTheme.typography.labelSmall)
                    Text(asalto.tirador1.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                
                if (!asalto.esBye) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ScoreInputEliminatoria(asalto.tocados1) { 
                            DataRepository.updateAsaltoCuadro(competitionId, nombreRonda, asalto.id, it, asalto.tocados2, asalto.terminado) 
                        }
                        Text("-", modifier = Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.headlineMedium)
                        ScoreInputEliminatoria(asalto.tocados2) { 
                            DataRepository.updateAsaltoCuadro(competitionId, nombreRonda, asalto.id, asalto.tocados1, it, asalto.terminado) 
                        }
                    }
                } else {
                    Text("PASO DIRECTO (BYE)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                }
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Tirador 2", style = MaterialTheme.typography.labelSmall)
                    Text(asalto.tirador2.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
            
            if (!asalto.esBye) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        DataRepository.updateAsaltoCuadro(competitionId, nombreRonda, asalto.id, asalto.tocados1, asalto.tocados2, !asalto.terminado)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (asalto.terminado) ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) 
                             else ButtonDefaults.buttonColors()
                ) {
                    Icon(
                        imageVector = if (asalto.terminado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (asalto.terminado) "Resultado Confirmado" else "Confirmar Resultado")
                }
            }
            
            if (asalto.terminado && !asalto.esBye) {
                val ganador = if (asalto.tocados1 >= asalto.tocados2) asalto.tirador1 else asalto.tirador2
                AssistChip(
                    onClick = {},
                    label = { Text("Ganador: ${ganador.nombre}") },
                    leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ScoreInputEliminatoria(value: Int, onValueChange: (Int) -> Unit) {
    Surface(
        onClick = { onValueChange((value + 1) % 16) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = value.toString(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
    }
}
