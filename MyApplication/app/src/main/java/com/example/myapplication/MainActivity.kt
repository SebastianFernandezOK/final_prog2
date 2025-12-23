package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "events") {
        composable(route = "events") {
            EventsScreen(
                onEventClick = { eventId ->
                    navController.navigate("events/detail/$eventId")
                },
                // Pasamos una función para ir al historial
                onHistoryClick = {
                    navController.navigate("sales")
                }
            )
        }
        composable(
            route = "events/detail/{eventId}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
            EventDetailScreen(
                eventId = eventId,
                navController = navController
            )
        }
        composable(
            route = "purchase/{eventId}/{seatRow}/{seatCol}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.IntType },
                navArgument("seatRow") { type = NavType.IntType },
                navArgument("seatCol") { type = NavType.IntType },
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
            val seatRow = backStackEntry.arguments?.getInt("seatRow") ?: 0
            val seatCol = backStackEntry.arguments?.getInt("seatCol") ?: 0
            PurchaseScreen(
                eventId = eventId,
                seatRow = seatRow,
                seatCol = seatCol,
                navController = navController
            )
        }
        // Nueva ruta para el historial de compras
        composable(route = "sales") {
            SalesScreen(navController = navController)
        }
    }
}
