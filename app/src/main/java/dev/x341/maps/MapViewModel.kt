package dev.x341.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.x341.maps.database.MarkerRepository
import dev.x341.maps.database.UserMarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val repository = MarkerRepository()

    private val _markers = MutableStateFlow<List<UserMarker>>(emptyList())
    val markers: StateFlow<List<UserMarker>> = _markers.asStateFlow()

    init {
        loadMarkers()
    }

    private fun loadMarkers() {
        viewModelScope.launch {
            val myMarkers = repository.getMarkers()
            _markers.value = myMarkers
        }
    }

    fun addMarker(lat: Double, lng: Double, title: String? = null, snippet: String? = null) {
        viewModelScope.launch {
            val success = repository.insertMarker(lat, lng, title, snippet)

            if (success) {
                loadMarkers()
            }
        }
    }
}