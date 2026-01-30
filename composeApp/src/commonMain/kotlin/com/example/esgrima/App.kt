package com.example.esgrima

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.esgrima.data.DataRepository
import com.example.esgrima.models.Role
import com.example.esgrima.ui.*

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val currentUser by DataRepository.currentUser.collectAsState()
        
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(onLoginSuccess = { _ ->
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            composable("dashboard") {
                DashboardScreen(onNavigate = { route ->
                    if (route == "logout") {
                        DataRepository.logout()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    } else {
                        navController.navigate(route)
                    }
                })
            }
            composable("fencers") {
                if (currentUser?.role == Role.ADMIN) {
                    FencersScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("referees") {
                if (currentUser?.role == Role.ADMIN) {
                    RefereesScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("competitions") {
                CompetitionsScreen(
                    onBack = { navController.popBackStack() },
                    onSelectCompetition = { navController.navigate("poules") }
                )
            }
            composable("poules") {
                PoulesScreen(onBack = { navController.popBackStack() })
            }
            composable("results") {
                ResultsSummaryScreen(onBack = { navController.popBackStack() })
            }
            composable("eliminatorias") {
                EliminatoriasScreen(onBack = { navController.popBackStack() })
            }
            composable("ranking") {
                RankingScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
