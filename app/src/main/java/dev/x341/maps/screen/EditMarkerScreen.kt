package dev.x341.maps.screen

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.x341.maps.MapViewModel
import dev.x341.maps.UploadState
import dev.x341.maps.permission.rememberCameraPermissionAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMarkerScreen(
    markerId: String,
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val markerList by viewModel.markers.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    val currentMarker = markerList.find { it.id == markerId }
    val uploadSequence by viewModel.uploadState.collectAsState()

    var title by remember(currentMarker) { mutableStateOf(currentMarker?.title ?: "") }
    var description by remember(currentMarker) { mutableStateOf(currentMarker?.snippet ?: "") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var isModifiable by remember { mutableStateOf(true) }

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

    LaunchedEffect(uploadSequence) {
        when (uploadState) {
            is UploadState.Success -> {
                Toast.makeText(context, "Marcador actualitzat amb èxit!", Toast.LENGTH_SHORT).show()
                viewModel.resetUploadState()
                onNavigateBack()
            }
            is UploadState.Error -> {
                Toast.makeText(context, "Error: ${(uploadState as UploadState.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.resetUploadState()
            }
            else -> {}
        }
    }

    if (currentMarker == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Marcador no trobat")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalls del Marcador") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Tornar Enrere")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isModifiable) "Mode Edició Activat" else "Només Lectura",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isModifiable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isModifiable,
                        onCheckedChange = { isModifiable = it },
                        enabled = uploadState !is UploadState.Loading
                    )
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Títol") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isModifiable && uploadState !is UploadState.Loading
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripció") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = isModifiable && uploadState !is UploadState.Loading
                )

                // LÓGICA DE IMÁGENES: Mostrar la nueva o la de Supabase
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Foto capturada",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else if (!currentMarker.image_url.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentMarker.image_url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto actual",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Botón de foto solo visible si es modificable
                if (isModifiable) {
                    Button(
                        onClick = launchCameraWithPermission,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uploadState !is UploadState.Loading
                    ) {
                        Text(if (capturedBitmap == null && currentMarker.image_url.isNullOrEmpty()) "Fer Foto" else "Canviar Foto")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón de guardar solo visible si es modificable
                if (isModifiable) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.deleteMarker(currentMarker.id!!)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = uploadState !is UploadState.Loading
                    ) {
                        Text("Eliminar Marcador")
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uploadState !is UploadState.Loading && title.isNotBlank(),
                        onClick = {
                            viewModel.updateMarker(
                                markerId = currentMarker.id!!,
                                title = title.trim(),
                                snippet = description.trim().takeIf { it.isNotBlank() },
                                newBitmap = capturedBitmap
                            )
                        }
                    ) {
                        Text("Actualitzar Marcador")
                    }
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
                        Text("Actualitzant marcador...")
                    }
                }
            }
        }
    }
}