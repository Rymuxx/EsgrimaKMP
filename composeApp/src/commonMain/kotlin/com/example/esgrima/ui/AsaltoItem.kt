package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Asalto

@Composable
fun AsaltoItem(competitionId: String, pouleId: String, asalto: Asalto) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (asalto.terminado) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(asalto.tirador1.nombre, fontWeight = if (asalto.tocados1 > asalto.tocados2) FontWeight.Bold else FontWeight.Normal)
                Text(asalto.tirador2.nombre, fontWeight = if (asalto.tocados2 > asalto.tocados1) FontWeight.Bold else FontWeight.Normal)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${asalto.tocados1} - ${asalto.tocados2}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }
        }
    }

    if (showDialog) {
        AsaltoEditDialog(
            asalto = asalto,
            onDismiss = { showDialog = false },
            onConfirm = { t1, t2 ->
                DataRepository.updateAsalto(competitionId, pouleId, asalto.id, t1, t2, true)
                showDialog = false
            }
        )
    }
}

@Composable
fun AsaltoEditDialog(asalto: Asalto, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    var t1 by remember { mutableIntStateOf(asalto.tocados1) }
    var t2 by remember { mutableIntStateOf(asalto.tocados2) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resultado del Asalto") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                    ScoreControl(label = asalto.tirador1.nombre, value = t1, limit = asalto.limiteTocados) { t1 = it }
                    Text("VS", modifier = Modifier.padding(horizontal = 16.dp))
                    ScoreControl(label = asalto.tirador2.nombre, value = t2, limit = asalto.limiteTocados) { t2 = it }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(t1, t2) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ScoreControl(label: String, value: Int, limit: Int, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 0) onValueChange(value - 1) }) { Text("-") }
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = { if (value < limit) onValueChange(value + 1) }) { Text("+") }
        }
    }
}
