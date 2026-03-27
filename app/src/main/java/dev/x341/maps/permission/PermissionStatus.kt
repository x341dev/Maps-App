package dev.x341.maps.permission

sealed class PermissionStatus {
    object Unknown : PermissionStatus()
    object Granted : PermissionStatus()
    object Denied : PermissionStatus()
    object ShowRationale : PermissionStatus()
}