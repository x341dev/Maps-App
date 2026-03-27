package dev.x341.maps.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    fun checkPermission(appPermission: AppPermission): PermissionStatus {
        val allGranted = appPermission.permission.all { permissionString ->
            ContextCompat.checkSelfPermission(
                context,
                permissionString
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            return PermissionStatus.Granted
        }

        val activity = context as? Activity
        val shouldShowRationale = activity?.let { act ->
            appPermission.permission.any { permissionString ->
                ActivityCompat.shouldShowRequestPermissionRationale(act, permissionString)
            }
        } ?: false

        return if (shouldShowRationale) {
            PermissionStatus.ShowRationale
        } else {
            PermissionStatus.Denied
        }
    }

    fun evaluateMapState(
        locationStatus: PermissionStatus,
        cameraStatus: PermissionStatus
    ): MapPermissionState {
        return when {
            locationStatus is PermissionStatus.Granted && cameraStatus is PermissionStatus.Granted -> {
                MapPermissionState.NavigateToMap
            }
            locationStatus is PermissionStatus.ShowRationale || cameraStatus is PermissionStatus.ShowRationale -> {
                MapPermissionState.ShowRationale
            }
            locationStatus is PermissionStatus.Denied || cameraStatus is PermissionStatus.Denied -> {
                MapPermissionState.ShowDenied
            }
            else -> {
                MapPermissionState.Requesting
            }
        }
    }
}