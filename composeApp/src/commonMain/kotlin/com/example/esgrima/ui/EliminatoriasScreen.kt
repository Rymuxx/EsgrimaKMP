package com.example.esgrima.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Asalto
import com.example.esgrima.models.FaseCompeticion
import com.example.esgrima.models.Role
import com.example.esgrima.models.Tirador
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EliminatoriasScreen(onBack: () -> Unit) {
    val selectedCompId by DataRepository.selectedCompetitionId.collectAsState()
    val competitions by DataRepository.competitions.collectAsState()
    val currentUser by DataRepository.currentUser.collectAsState()
    val comp = competitions.find { it.id == selectedCompId }
    
    var showConfigDialog by remember { mutableStateOf(false) }
    var corteInput by remember { mutableStateOf(comp?.numClasificadosCorte?.toString() ?: "16") }

    val canAdvance = remember(comp) {
        val lastRonda = comp?.rondasEliminatorias?.lastOrNull()
        lastRonda != null && lastRonda.asaltos.all { it.terminado } && comp.fase != FaseCompeticion.FINALIZADA
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(comp?.let { "Tabla - ${it.nombre}" } ?: "TablaEliminatorio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (currentUser?.role == Role.ADMIN && comp != null && comp.rondasEliminatorias.isNotEmpty()) {
                        if (comp.fase != FaseCompeticion.FINALIZADA) {
                            IconButton(onClick = { DataRepository.simulateFullEliminatorias(comp.id) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Simular Todo")
                            }
                            if (canAdvance) {
                                IconButton(onClick = { DataRepository.avanzarCuadro(comp.id) }) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente Ronda")
                                }
                            }
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
                    Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("El cuadro no ha sido generado.")
                    if (currentUser?.role == Role.ADMIN) {
                        Button(onClick = { showConfigDialog = true }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Configurar Corte y Generar Tabla")
                        }
                    }
                }
            }
        } else {
            // SOLUCIÓN AL SCROLL: Contenedores anidados para scroll 2D independiente
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Scroll Vertical Externo
                ) {
                    Box(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState()) // Scroll Horizontal Interno
                    ) {
                        Row(modifier = Modifier.padding(32.dp)) {
                            comp.rondasEliminatorias.forEachIndexed { rondaIdx, ronda ->
                                val isLastRonda = rondaIdx == comp.rondasEliminatorias.size - 1
                                
                                Column(
                                    modifier = Modifier.width(220.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = ronda.nombre.uppercase(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    )

                                    // Lógica de espaciado FIE
                                    val baseHeight = 100.dp
                                    val multiplier = 2.0.pow(rondaIdx).toFloat()
                                    val verticalSpacing = baseHeight * (multiplier - 1f)
                                    val initialPadding = (baseHeight * (multiplier - 1f)) / 2f

                                    Spacer(modifier = Modifier.height(initialPadding))

                                    ronda.asaltos.forEachIndexed { asaltoIdx, asalto ->
                                        Box(contentAlignment = Alignment.Center) {
                                            TableauMatchCard(comp.id, ronda.nombre, asalto, currentUser?.role == Role.ADMIN)
                                            
                                            if (!isLastRonda) {
                                                val isUpper = asaltoIdx % 2 == 0
                                                // Ajustamos la altura del conector para que llegue al centro del siguiente grupo
                                                MatchConnector(isUpper, verticalSpacing + baseHeight)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(verticalSpacing + 24.dp))
                                    }
                                }
                                if (!isLastRonda) {
                                    Spacer(modifier = Modifier.width(40.dp))
                                }
                            }
                            
                            if (comp.fase == FaseCompeticion.FINALIZADA) {
                                ChampionView(comp.rondasEliminatorias.last().asaltos.firstOrNull())
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfigDialog && comp != null) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = { Text("Configurar Tabla") },
            text = {
                Column {
                    Text("Tiradores clasificados:")
                    OutlinedTextField(
                        value = corteInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) corteInput = it },
                        label = { Text("Ej: 8, 16, 24, 32") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val n = corteInput.toIntOrNull() ?: 16
                    DataRepository.generarCuadroEliminatorio(comp.id, n)
                    showConfigDialog = false
                }) { Text("Generar") }
            }
        )
    }
}

@Composable
fun TableauMatchCard(competitionId: String, nombreRonda: String, asalto: Asalto, canEdit: Boolean) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.width(200.dp).clickable(enabled = canEdit && !asalto.esBye) { showEditDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (asalto.terminado) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            FencerRow(asalto.tirador1, asalto.tocados1, asalto.ganadorId == asalto.tirador1.id, asalto.terminado)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.LightGray)
            FencerRow(asalto.tirador2, asalto.tocados2, asalto.ganadorId == asalto.tirador2.id, asalto.terminado)
        }
    }

    if (showEditDialog) {
        AsaltoEditDialog(
            asalto = asalto,
            onDismiss = { showEditDialog = false },
            onConfirm = { t1, t2 ->
                DataRepository.updateAsaltoCuadro(competitionId, nombreRonda, asalto.id, t1, t2, true)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun FencerRow(tirador: Tirador, tocados: Int, esGanador: Boolean, terminado: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (tirador.id == "bye") "PASO LIBRE" else tirador.nombre,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (esGanador) FontWeight.Black else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = if (esGanador && terminado) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
        Text(
            text = if (terminado || tocados > 0) tocados.toString() else "-",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun MatchConnector(isUpper: Boolean, height: Dp) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(20.dp) // Altura mínima para que el Box exista en el layout
            .drawBehind {
                val startX = 0f
                val endX = size.width
                val midY = size.height / 2
                
                // Línea horizontal saliendo de la tarjeta
                drawLine(color, Offset(startX, midY), Offset(endX / 2, midY), strokeWidth = 2.dp.toPx())
                
                // Línea vertical de unión (hacia arriba o hacia abajo)
                val verticalDist = (height / 2).toPx()
                val verticalEndY = if (isUpper) midY + verticalDist else midY - verticalDist
                drawLine(color, Offset(endX / 2, midY), Offset(endX / 2, verticalEndY), strokeWidth = 2.dp.toPx())
                
                // Línea horizontal hacia la siguiente ronda (solo se dibuja una por pareja)
                if (isUpper) {
                    drawLine(color, Offset(endX / 2, verticalEndY), Offset(endX, verticalEndY), strokeWidth = 2.dp.toPx())
                }
            }
    )
}

@Composable
fun ChampionView(finalMatch: Asalto?) {
    if (finalMatch == null || !finalMatch.terminado) return
    
    val ganador = if (finalMatch.tocados1 > finalMatch.tocados2) finalMatch.tirador1 else finalMatch.tirador2
    
    Column(
        modifier = Modifier.width(200.dp).padding(start = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CAMPEÓN", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                ganador.nombre,
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
