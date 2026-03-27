package dev.x341.maps

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.x341.maps.database.MarkerRepository
import dev.x341.maps.database.UserMarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}

class MapViewModel : ViewModel() {
    private val repository = MarkerRepository()

    private val _markers = MutableStateFlow<List<UserMarker>>(emptyList())
    val markers: StateFlow<List<UserMarker>> = _markers.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    init {
        loadMarkers()
    }

    fun loadMarkers() {
        viewModelScope.launch {
            val myMarkers = repository.getMarkers()
            _markers.value = myMarkers
        }
    }

    fun addMarkerWithImage(
        lat: Double,
        lng: Double,
        title: String? = null,
        snippet: String? = null,
        bitmap: android.graphics.Bitmap
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading

            val success = repository.uploadAndSaveMarker(
                lat = lat,
                lng = lng,
                title = title,
                snippet = snippet,
                bitmap = bitmap
            )

            if (success) {
                _uploadState.value = UploadState.Success
                loadMarkers()
            } else {
                _uploadState.value = UploadState.Error("Failed to upload marker")
            }
        }
    }

    fun updateMarker(
        markerId: String,
        title: String?,
        snippet: String?,
        newBitmap: Bitmap?
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading

            val currentMarker = _markers.value.find { it.id == markerId }

            if (currentMarker != null) {
                val success = repository.updateMarker(
                    markerId = markerId,
                    lat = currentMarker.latitude,
                    lng = currentMarker.longitude,
                    title = title,
                    snippet = snippet,
                    bitmap = newBitmap,
                    existingImageUrl = currentMarker.image_url
                )

                if (success) {
                    _uploadState.value = UploadState.Success
                    loadMarkers()
                } else {
                    _uploadState.value = UploadState.Error("Error updating marker")
                }
            } else {
                _uploadState.value = UploadState.Error("Marker not found")
            }
        }
    }

    fun getItbMarker(): UserMarker {
        return UserMarker(
            id = "itb",
            user_id = "system",
            latitude = 41.45357483065934,
            longitude = 2.1861210702710827,
            title = "ITB",
            snippet = "Institut Tecnològic de Barcelona",
        )
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}