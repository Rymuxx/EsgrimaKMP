package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
fun PoulesScreen(onBack: () -> Unit) {
    val selectedCompId by DataRepository.selectedCompetitionId.collectAsState()
    val competitions by DataRepository.competitions.collectAsState()
    val comp = competitions.find { it.id == selectedCompId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(comp?.let { "Poules - ${it.nombre}" } ?: "Gestión de Poules") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (comp == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Selecciona una competición en el menú de Competiciones.")
            }
        } else if (comp.poules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay poules generadas para esta competición.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { DataRepository.createPoules(comp.id, 2) }) {
                        Text("Generar Poules Automáticamente")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(comp.poules) { poule ->
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

@Composable
fun AsaltoItem(compId: String, pouleId: String, asalto: Asalto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Información del Árbitro
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
                // Tirador 1
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tirador 1", style = MaterialTheme.typography.labelSmall)
                    Text(asalto.tirador1.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(asalto.tirador1.club.nombre, style = MaterialTheme.typography.bodySmall)
                }
                
                // Marcador
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScoreInput(asalto.tocados1) { 
                        DataRepository.updateAsalto(compId, pouleId, asalto.id, it, asalto.tocados2, asalto.terminado) 
                    }
                    Text("-", modifier = Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.headlineMedium)
                    ScoreInput(asalto.tocados2) { 
                        DataRepository.updateAsalto(compId, pouleId, asalto.id, asalto.tocados1, it, asalto.terminado) 
                    }
                }
                
                // Tirador 2
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Tirador 2", style = MaterialTheme.typography.labelSmall)
                    Text(asalto.tirador2.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(asalto.tirador2.club.nombre, style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botón de Estado
            Button(
                onClick = {
                    DataRepository.updateAsalto(compId, pouleId, asalto.id, asalto.tocados1, asalto.tocados2, !asalto.terminado)
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
                Text(if (asalto.terminado) "Asalto Finalizado" else "Marcar como Finalizado")
            }
        }
    }
}

@Composable
fun ScoreInput(value: Int, onValueChange: (Int) -> Unit) {
    Surface(
        onClick = { onValueChange((value + 1) % 6) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = value.toString(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black
        )
    }
}
