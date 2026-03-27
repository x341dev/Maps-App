package dev.x341.maps.permission

import android.Manifest

sealed class AppPermission(val permission: List<String>) {
    object Camera : AppPermission(
        listOf(Manifest.permission.CAMERA)
    )

    object Location : AppPermission(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
}