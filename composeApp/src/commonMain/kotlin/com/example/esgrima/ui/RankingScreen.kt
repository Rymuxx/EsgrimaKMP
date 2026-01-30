package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(onBack: () -> Unit) {
    val selectedCompId by DataRepository.selectedCompetitionId.collectAsState()
    val competitions by DataRepository.competitions.collectAsState()
    val comp = competitions.find { it.id == selectedCompId }
    
    val ranking = comp?.let { DataRepository.getRankingCalculado(it) } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(comp?.let { "Clasificación - ${it.nombre}" } ?: "Clasificación General") },
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
                Text("Selecciona una competición primero.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("Pos", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                        Text("Tirador", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("% Vict.", modifier = Modifier.width(70.dp), fontWeight = FontWeight.Bold)
                        Text("Ind.", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold)
                        Text("TD", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                }
                itemsIndexed(ranking) { _, item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("${item.posicion}", modifier = Modifier.width(40.dp))
                        Text(item.tirador.nombre, modifier = Modifier.weight(1f))
                        
                        // Cálculo del porcentaje de victorias
                        val percentage = (item.v_m * 100).toInt()
                        Text("$percentage%", modifier = Modifier.width(70.dp))

                        Text("${if (item.indice > 0) "+" else ""}${item.indice}", modifier = Modifier.width(50.dp))
                        Text("${item.td}", modifier = Modifier.width(40.dp))
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
