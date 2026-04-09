package dev.x341.maps.navigation

sealed class Routes(val route: String) {
    data object MapScreen : Routes("MapScreen")
    data object ListScreen : Routes("ListScreen")

    data object AddMarkerScreen : Routes("AddMarkerScreen/{lat}/{lng}") {
        fun createRoute(lat: Double, lng: Double) = "AddMarkerScreen/$lat/$lng"
    }

    data object EditMarkerScreen : Routes("EditMarkerScreen/{markerId}") {
        fun createRoute(markerId: String) = "EditMarkerScreen/$markerId"
    }
}