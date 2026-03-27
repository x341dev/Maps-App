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
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.rememberMarkerState
import dev.x341.maps.MapViewModel
import dev.x341.maps.component.MarkerCard
import dev.x341.maps.component.TempMarkerCard


@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel,
    onNavigateToAdd: (LatLng) -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val markerList by viewModel.markers.collectAsState()

    var tempMarkPos by remember { mutableStateOf<LatLng?>(null) }

    val itb = LatLng(41.45357483065934, 2.1861210702710827)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(itb, 15f)
    }

    LaunchedEffect(Unit) {
        viewModel.loadMarkers()
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
            MarkerInfoWindow(
                state = rememberUpdatedMarkerState(position = itb),
            ) {
                MarkerCard(
                    markerData = viewModel.getItbMarker()
                )
            }

            markerList.forEach { marker ->
                val pos = LatLng(marker.latitude, marker.longitude)

                key(marker.id ?: marker.latitude.toString()) {
                    val markerState = rememberUpdatedMarkerState(pos)

                    MarkerInfoWindow(
                        state = markerState,
                        onInfoWindowClick = {
                            marker.id?.let { id ->
                                onNavigateToEdit(id)
                            }
                        }
                    ) {
                        MarkerCard(markerData = marker)
                    }
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
                        onNavigateToAdd(pos)
                    }
                ) {
                    TempMarkerCard(lat = pos.latitude, lng = pos.longitude)
                }
            }
        }
    }
}