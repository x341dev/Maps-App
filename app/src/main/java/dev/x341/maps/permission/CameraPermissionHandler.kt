package dev.x341.maps.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberCameraPermissionAction(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionRationale: () -> Unit = onPermissionDenied
): () -> Unit {
    val context = LocalContext.current
    val permissionManager = remember(context) { PermissionManager(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    return {
        when (permissionManager.checkPermission(AppPermission.Camera)) {
            is PermissionStatus.Granted -> onPermissionGranted()
            is PermissionStatus.ShowRationale -> {
                onPermissionRationale()
                permissionLauncher.launch(AppPermission.Camera.permission.first())
            }
            else -> permissionLauncher.launch(AppPermission.Camera.permission.first())
        }
    }
}

