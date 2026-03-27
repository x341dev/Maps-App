package dev.x341.maps.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.maps.model.LatLng
import dev.x341.maps.MapViewModel
import dev.x341.maps.database.SupabaseClient
import dev.x341.maps.screen.AddMarkerScreen
import dev.x341.maps.screen.EditMarkerScreen
import dev.x341.maps.screen.MapScreen
import io.github.jan.supabase.auth.auth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sharedViewModel: MapViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var isAuthReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val session = SupabaseClient.client.auth.currentSessionOrNull()

            if (session == null) {
                SupabaseClient.client.auth.signInAnonymously()
            }

            isAuthReady = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (!isAuthReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = Routes.MapScreen.route
    ) {
        composable(route = Routes.MapScreen.route) {
            MapScreen(
                viewModel = sharedViewModel,
                onNavigateToAdd = { latLng ->
                    val route = Routes.AddMarkerScreen.createRoute(latLng.latitude, latLng.longitude)
                    navController.navigate(route)
                },
                onNavigateToEdit = { markerId ->
                    val route = Routes.EditMarkerScreen.createRoute(markerId)
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = Routes.AddMarkerScreen.route,
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val latStr = backStackEntry.arguments?.getString("lat") ?: "0.0"
            val lngStr = backStackEntry.arguments?.getString("lng") ?: "0.0"
            val latLng = LatLng(latStr.toDoubleOrNull() ?: 0.0, lngStr.toDoubleOrNull() ?: 0.0)

            AddMarkerScreen(
                latLng = latLng,
                viewModel = sharedViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EditMarkerScreen.route,
            arguments = listOf(
                navArgument("markerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val markerId = backStackEntry.arguments?.getString("markerId") ?: return@composable

            EditMarkerScreen(
                markerId = markerId,
                viewModel = sharedViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}