package dev.x341.maps.screen

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import dev.x341.maps.MapViewModel
import dev.x341.maps.UploadState
import dev.x341.maps.permission.rememberCameraPermissionAction

@Composable
fun AddMarkerScreen(
    latLng: LatLng,
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val uploadState by viewModel.uploadState.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        capturedBitmap = bitmap
    }

    val launchCameraWithPermission = rememberCameraPermissionAction(
        onPermissionGranted = {
            runCatching { cameraLauncher.launch(null) }
                .onFailure {
                    Toast.makeText(context, "No s'ha pogut obrir la camera", Toast.LENGTH_SHORT).show()
                }
        },
        onPermissionDenied = {
            Toast.makeText(context, "Cal permisos de camera per fer una foto", Toast.LENGTH_SHORT).show()
        },
        onPermissionRationale = {
            Toast.makeText(context, "Cal permisos de camera per fer una foto", Toast.LENGTH_SHORT).show()
        }
    )

    LaunchedEffect(Unit) {
        viewModel.resetUploadState()
    }

    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadState.Success -> {
                Toast.makeText(context, "Marcador desat amb exit!", Toast.LENGTH_SHORT).show()
                viewModel.resetUploadState()
                onNavigateBack()
            }

            is UploadState.Error -> {
                Toast.makeText(
                    context,
                    "Error al desar el marcador: ${(uploadState as UploadState.Error).message}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetUploadState()
            }

            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Coordenades: ${"%.4f".format(latLng.latitude)}, ${"%.4f".format(latLng.longitude)}")

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titol") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uploadState !is UploadState.Loading
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripcio") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = uploadState !is UploadState.Loading
            )

            capturedBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto capturada",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Button(
                onClick = launchCameraWithPermission,
                modifier = Modifier.fillMaxWidth(),
                enabled = uploadState !is UploadState.Loading
            ) {
                Text(if (capturedBitmap == null) "Fer Foto" else "Canviar Foto")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = uploadState !is UploadState.Loading &&
                    title.isNotBlank() &&
                    capturedBitmap != null,
                onClick = {
                    capturedBitmap?.let { bitmap ->
                        viewModel.addMarkerWithImage(
                            lat = latLng.latitude,
                            lng = latLng.longitude,
                            title = title.trim(),
                            snippet = description.trim().takeIf { it.isNotBlank() },
                            bitmap = bitmap
                        )
                    }
                }
            ) {
                Text("Desar Marcador")
            }
        }

        if (uploadState is UploadState.Loading) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Desant marcador...")
                }
            }
        }
    }
}