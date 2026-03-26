package dev.x341.maps.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.maps.android.compose.MarkerInfoWindow
import dev.x341.maps.MapViewModel
import dev.x341.maps.component.MarkerCard
import dev.x341.maps.component.TempMarkerCard


@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
    onNavigate: (LatLng) -> Unit
) {
    val markerList by viewModel.markers.collectAsState()

    var tempMarkPos by remember { mutableStateOf<LatLng?>(null) }

    val itb = LatLng(41.45357483065934, 2.1861210702710827)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(itb, 15f)
    }

    Column(modifier.fillMaxSize()) {
        GoogleMap(
            modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                tempMarkPos = null
            }, onMapLongClick = { latLnt ->
                tempMarkPos = latLnt
            }
        ) {
            Marker(
                state = rememberUpdatedMarkerState(position = itb),
                title = "ITB",
                snippet = "Marker at ITB"
            )

            markerList.forEach { marker ->
                val pos = LatLng(marker.latitude, marker.longitude)
                MarkerInfoWindow(
                    state = rememberUpdatedMarkerState(position = pos)
                ) {
                    MarkerCard(markerData = marker)
                }
            }

            tempMarkPos?.let { pos ->

                val markerState = rememberUpdatedMarkerState(position = pos)

                LaunchedEffect(markerState) {
                    markerState.showInfoWindow()
                }

                MarkerInfoWindow(
                    state = markerState,
                    visible = true,
                    onInfoWindowClick = {
                        tempMarkPos = null
                        onNavigate(pos)
                    }
                ) {
                    TempMarkerCard(lat = pos.latitude, lng = pos.longitude)
                }
            }
        }
    }
}