package dev.x341.maps.screen

import android.media.ImageReader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.x341.maps.MapViewModel
import dev.x341.maps.database.UserMarker

@Composable
fun ListScreen(
    viewModel: MapViewModel
) {
    val context = LocalContext.current

    val markers by viewModel.markers.collectAsState(initial = emptyList())

    Column(modifier = Modifier
        .fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            markers.forEach { marker ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            marker.title?.let { Text(text = it, modifier = Modifier.width(200.dp)) }
                            Text(text = "Lat: ${marker.latitude}, Lng: ${marker.longitude}")
                        }

                        if (!marker.image_url.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(marker.image_url)
                                    .allowHardware(false)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Marker Image",
                                modifier = Modifier                                .width(100.dp)
                                    .align(Alignment.CenterVertically),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}