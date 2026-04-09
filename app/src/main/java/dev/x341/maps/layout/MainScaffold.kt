package dev.x341.maps.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.x341.maps.component.DrawerMenu
import dev.x341.maps.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavController, content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val isDetailScreen = currentRoute == Routes.AddMarkerScreen.route ||
        currentRoute == Routes.EditMarkerScreen.route

    val topBarTitle = when (currentRoute) {
        Routes.ListScreen.route -> "Llista de Marcadors"
        Routes.AddMarkerScreen.route -> "Nou Marcador"
        Routes.EditMarkerScreen.route -> "Detalls del Marcador"
        else -> "Maps App"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isDetailScreen,
        drawerContent = {
            DrawerMenu(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.MapScreen.route) { inclusive = false }
                        launchSingleTop = true
                    }
                    scope.launch { drawerState.close() }
                }
            )
        }) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(text = topBarTitle) },
                navigationIcon = {
                    if (isDetailScreen) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Tornar Enrere")
                        }
                    } else {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                }
            )
        }) { padding ->
            Box(Modifier.padding(padding)) { content() }
        }
    }
}
