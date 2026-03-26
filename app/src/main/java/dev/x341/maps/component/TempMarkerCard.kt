package dev.x341.maps.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

fun markerBubbleShape(cornerRadiusPx: Float, arrowSizePx: Float) = GenericShape { size, _ ->
    val rectHeight = size.height - arrowSizePx
    val rectWidth = size.width
    val corner = cornerRadiusPx
    val arrow = arrowSizePx

    moveTo(corner, 0f)
    lineTo(rectWidth - corner, 0f)
    arcTo(
        rect = Rect(rectWidth - corner * 2, 0f, rectWidth, corner * 2),
        startAngleDegrees = -90f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )
    lineTo(rectWidth, rectHeight - corner)
    arcTo(
        rect = Rect(rectWidth - corner * 2, rectHeight - corner * 2, rectWidth, rectHeight),
        startAngleDegrees = 0f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )

    lineTo(rectWidth / 2 + arrow, rectHeight)
    lineTo(rectWidth / 2, size.height)
    lineTo(rectWidth / 2 - arrow, rectHeight)

    lineTo(corner, rectHeight)
    arcTo(
        rect = Rect(0f, rectHeight - corner * 2, corner * 2, rectHeight),
        startAngleDegrees = 90f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )
    lineTo(0f, corner)
    arcTo(
        rect = Rect(0f, 0f, corner * 2, corner * 2),
        startAngleDegrees = 180f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )
    close()
}

@Composable
fun TempMarkerCard(
    lat: Double,
    lng: Double
) {
    val backgroundColor = MaterialTheme.colorScheme.surface

    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { 12.dp.toPx() }
    val arrowSizePx = with(density) { 12.dp.toPx() }

    val shape = remember(cornerRadiusPx, arrowSizePx) {
        markerBubbleShape(cornerRadiusPx, arrowSizePx)
    }

    Box(
        modifier = Modifier
            .width(230.dp)
            .defaultMinSize(minHeight = 120.dp)
            .background(color = backgroundColor, shape = shape)
            .clip(shape)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp + 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add new marker at:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lat: ${"%.4f".format(lat)}\nLng: ${"%.4f".format(lng)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {}, // Decorative
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Marker")
            }
        }
    }
}