package dev.x341.maps.screen

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import dev.x341.maps.MapViewModel
import dev.x341.maps.UploadState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarkerScreen(
    latLng: LatLng,
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val uploadState by viewModel.uploadState.collectAsState()

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        capturedBitmap = bitmap
    }

    LaunchedEffect(Unit) {
        viewModel.resetUploadState()
    }

    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadState.Success -> {
                Toast.makeText(context, "Marcador desat amb èxit!", Toast.LENGTH_SHORT).show()
                viewModel.resetUploadState()
                onNavigateBack()
            }
            is UploadState.Error -> {
                Toast.makeText(context, "Error al desar el marcador: ${(uploadState as UploadState.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.resetUploadState()
            }
            else -> {} // No action needed for Idle or Loading states here
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nou Marcador") },
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
                Text(text = "Coordenades: ${"%.4f".format(latLng.latitude)}, ${"%.4f".format(latLng.longitude)}")

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Títol") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uploadState !is UploadState.Loading
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripció") },
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
                    onClick = { cameraLauncher.launch(null) },
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
}