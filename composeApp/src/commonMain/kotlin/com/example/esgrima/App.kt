package com.example.esgrima

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.esgrima.data.DataRepository
import com.example.esgrima.ui.*

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val usuarioActual by DataRepository.currentUser.collectAsState()
        
        NavHost(navController = navController, startDestination = if (usuarioActual == null) "login" else "dashboard") {
            composable("login") {
                LoginScreen(onLoginSuccess = {
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
            composable("tiradores") {
                TiradoresScreen(onBack = { navController.popBackStack() })
            }
            composable("arbitros") {
               RefereesScreen(onBack = { navController.popBackStack() })
            }
            composable("ranking") {
                RankingScreen(onBack = { navController.popBackStack() })
            }
            composable("competitions") {
                CompetitionsScreen(
                    onBack = { navController.popBackStack() },
                    onSelectCompetition = { id -> 
                        navController.navigate("competition_detail/$id") 
                    }
                )
            }
            composable(
                "competition_detail/{competitionId}",
                arguments = listOf(navArgument("competitionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val competitionId = backStackEntry.arguments?.getString("competitionId")
                if (competitionId != null) {
                    CompetitionDetailScreen(
                        competitionId = competitionId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
