package com.example.esgrima.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Competicion
import com.example.esgrima.models.Poule
import com.example.esgrima.models.Tirador

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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: Competición no encontrada")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(comp.poules) { poule ->
                    PouleMatrix(comp, poule)
                }
            }
        }
    }
}

@Composable
fun PouleMatrix(comp: Competicion, poule: Poule) {
    val fencers = (poule.asaltos.map { it.tirador1 } + poule.asaltos.map { it.tirador2 }).distinctBy { it.id }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).horizontalScroll(rememberScrollState())) {
            Text(poule.nombre, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            // Header row
            Row {
                Box(modifier = Modifier.width(150.dp).height(40.dp))
                fencers.forEachIndexed { index, _ ->
                    Cell(text = "${index + 1}", isHeader = true)
                }
                listOf("V", "M", "TD", "TR", "Ind").forEach {
                    Cell(text = it, isHeader = true, color = MaterialTheme.colorScheme.secondaryContainer)
                }
            }

            // Fencer rows
            fencers.forEachIndexed { i, fencer ->
                Row {
                    Text(
                        text = "${i + 1}. ${fencer.nombre}",
                        modifier = Modifier.width(150.dp).height(40.dp).padding(4.dp),
                        maxLines = 1,
                        fontSize = 12.sp
                    )
                    
                    fencers.forEachIndexed { j, oponente ->
                        if (i == j) {
                            Box(modifier = Modifier.size(40.dp).background(Color.DarkGray))
                        } else {
                            val asalto = poule.asaltos.find { 
                                (it.tirador1.id == fencer.id && it.tirador2.id == oponente.id) ||
                                (it.tirador2.id == fencer.id && it.tirador1.id == oponente.id)
                            }
                            
                            val score = if (asalto == null) "" 
                            else if (!asalto.terminado) "-"
                            else if (asalto.tirador1.id == fencer.id) {
                                if (asalto.tocados1 == 5) "V" else asalto.tocados1.toString()
                            } else {
                                if (asalto.tocados2 == 5) "V" else asalto.tocados2.toString()
                            }

                            Box(
                                modifier = Modifier.size(40.dp).border(0.5.dp, Color.LightGray).clickable {
                                    // Aquí se abriría un diálogo para anotar el asalto
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(score, fontWeight = if (score == "V") FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    // Stats for the fencer in this poule
                    val matches = poule.asaltos.filter { it.terminado && (it.tirador1.id == fencer.id || it.tirador2.id == fencer.id) }
                    val wins = matches.count { (it.tirador1.id == fencer.id && it.tocados1 > it.tocados2) || (it.tirador2.id == fencer.id && it.tocados2 > it.tocados1) }
                    val td = matches.sumOf { if (it.tirador1.id == fencer.id) it.tocados1 else it.tocados2 }
                    val tr = matches.sumOf { if (it.tirador1.id == fencer.id) it.tocados2 else it.tocados1 }
                    
                    Cell(text = wins.toString())
                    Cell(text = matches.size.toString())
                    Cell(text = td.toString())
                    Cell(text = tr.toString())
                    Cell(text = (td - tr).toString())
                }
            }
        }
    }
}

@Composable
fun Cell(text: String, isHeader: Boolean = false, color: Color = Color.Transparent) {
    Box(
        modifier = Modifier.size(40.dp).background(color).border(0.5.dp, Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 14.sp else 12.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
        )
    }
}
