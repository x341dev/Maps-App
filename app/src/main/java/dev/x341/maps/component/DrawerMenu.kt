package dev.x341.maps.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DrawerMenu(
    currentRoute: String?, onNavigate: (String) -> Unit
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Menu")
        Spacer(modifier = Modifier.height(16.dp))
        DrawerItem.entries.forEach { item ->
            val selected = currentRoute == item.routes.route
            NavigationDrawerItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.text) },
                label = { Text(text = item.text) },
                selected = selected,
                onClick = { onNavigate(item.routes.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}