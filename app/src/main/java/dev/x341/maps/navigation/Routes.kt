package dev.x341.maps.navigation

sealed class Routes (val route: String) {
    object MapScreen: Routes("MapScreen")
    object ListScreen: Routes("ListScreen")
    object AddMarkerScreen: Routes("AddMarkerScreen/{lat}/{lng}") {
        fun createRoute(lat: Double, lng: Double) = "AddMarkerScreen/$lat/$lng"
    }
        object EditMarkerScreen: Routes("EditMarkerScreen/{markerId}") {
            fun createRoute(markerId: String) = "EditMarkerScreen/$markerId"
        }
}