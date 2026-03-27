package dev.x341.maps.permission

sealed class MapPermissionState {
    object Requesting : MapPermissionState()
    object ShowDenied : MapPermissionState()
    object ShowRationale : MapPermissionState()
    object NavigateToMap : MapPermissionState()
}