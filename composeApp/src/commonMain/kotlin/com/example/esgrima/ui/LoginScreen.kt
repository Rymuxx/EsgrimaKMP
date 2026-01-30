package com.example.esgrima.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.esgrima.data.DataRepository

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Esgrima Pro", style = MaterialTheme.typography.headlineLarge)
        Text("Acceso Seguro", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; errorMessage = null },
            label = { Text("Usuario") },
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { 
                if (password.length < 6) {
                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                } else if (DataRepository.login(username, password)) {
                    onLoginSuccess(username)
                } else {
                    errorMessage = "Usuario o contraseña incorrectos"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Credenciales de prueba:", style = MaterialTheme.typography.labelLarge)
                Text("Admin: admin / admin123", style = MaterialTheme.typography.bodySmall)
                Text("Árbitro: arbitro / arbitro123", style = MaterialTheme.typography.bodySmall)
                Text("Tirador: tirador / tirador123", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
