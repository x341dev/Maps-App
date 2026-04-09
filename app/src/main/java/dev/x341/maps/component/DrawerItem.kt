package dev.x341.maps.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.ui.graphics.vector.ImageVector
import dev.x341.maps.navigation.Routes

enum class DrawerItem(
    val icon: ImageVector,
    val text: String,
    val routes: Routes
) {
    MAP(Icons.Default.Create, "Map", Routes.MapScreen),
    LIST(Icons.Default.Create, "List", Routes.ListScreen)
}