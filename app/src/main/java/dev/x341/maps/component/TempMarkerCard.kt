package dev.x341.maps.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TempMarkerCard(
    lat: Double,
    lng: Double
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Text(
                text = "Add new marker at: ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp)
            )

            Text(
                text = "Lat: ${"%.4f".format(lat)}, Lng: ${"%.4f".format(lng)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Button( // Decorative button, no actual functionality
                onClick = { },
                modifier = Modifier
                    .padding(12.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
            ) {
                Text("Add Marker")
            }
        }
    }
}