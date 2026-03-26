package dev.x341.maps.navigation

sealed class Routes (val route: String) {
    object MapScreen: Routes("MapScreen")
    object AddMarkerScreen: Routes("AddMarkerScreen/{lat}/{lng}") {
        fun createRoute(lat: Double, lng: Double) = "AddMarkerScreen/$lat/$lng"
    }
}