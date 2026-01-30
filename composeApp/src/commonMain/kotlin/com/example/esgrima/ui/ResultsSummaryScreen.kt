package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Competicion
import com.example.esgrima.models.FaseCompeticion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsSummaryScreen(onBack: () -> Unit) {
    val competitions by DataRepository.competitions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Competiciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (competitions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay competiciones registradas.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(competitions) { comp ->
                    CompetitionSummaryCard(comp)
                }
            }
        }
    }
}

@Composable
fun CompetitionSummaryCard(comp: Competicion) {
    val ranking = DataRepository.getRankingCalculado(comp)
    val winner = comp.rondasEliminatorias.lastOrNull()?.asaltos?.find { it.terminado && it.id.contains("Final", ignoreCase = true) }?.let { asalto ->
        if (asalto.tocados1 >= asalto.tocados2) asalto.tirador1 else asalto.tirador2
    } ?: ranking.firstOrNull()?.tirador

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(comp.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${comp.arma} - ${comp.lugar} (${comp.fecha})", style = MaterialTheme.typography.bodyMedium)
                }
                Badge(
                    containerColor = when(comp.fase) {
                        FaseCompeticion.FINALIZADA -> MaterialTheme.colorScheme.primary
                        FaseCompeticion.ELIMINATORIAS -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                ) {
                    Text(comp.fase.name, modifier = Modifier.padding(4.dp))
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Text("Podio Provisional / Final:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ranking.take(3).forEachIndexed { index, clas ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = when(index) {
                            0 -> androidx.compose.ui.graphics.Color(0xFFFFD700)
                            1 -> androidx.compose.ui.graphics.Color(0xFFC0C0C0)
                            else -> androidx.compose.ui.graphics.Color(0xFFCD7F32)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${index + 1}º ${clas.tirador.nombre}", style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            if (comp.rondasEliminatorias.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Última Ronda: ${comp.rondasEliminatorias.last().nombre}", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}
