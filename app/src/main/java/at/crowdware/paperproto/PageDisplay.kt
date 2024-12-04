package at.crowdware.paperproto

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import kotlin.math.max

@Composable
fun ImageWithMultipleHotspots(
    page: Page,
    onHotspotsChanged: (List<HotSpot>) -> Unit) {
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberImagePainter(Uri.parse(page.picture)),
            contentDescription = "Image with hotspots",
            modifier = Modifier.fillMaxSize()
        )

        page.hotSpots.forEachIndexed { index, hotspot ->
            HotspotBox(
                hotspot = hotspot,
                onMove = { newPosition ->
                    println("New position: $newPosition")
                    val updatedHotspots = page.hotSpots.toMutableList().apply {
                        this[index] = this[index].copy(
                            x = newPosition.x.toInt(),
                            y = newPosition.y.toInt()
                        )
                    }
                    onHotspotsChanged(updatedHotspots)
                },
                onResize = { newSize, offset ->
                    val updatedHotspots = page.hotSpots.toMutableList().apply {
                        this[index] = this[index].copy(
                            x = offset.x.toInt(),
                            y = offset.y.toInt(),
                            width = max(newSize.width.toInt(), 50),  // Minimum width
                            height = max(newSize.height.toInt(), 50) // Minimum height
                        )
                    }
                    onHotspotsChanged(updatedHotspots)
                },
                onDelete = {
                    val updatedHotspots = page.hotSpots.toMutableList().apply {
                        removeAt(index)
                    }
                    onHotspotsChanged(updatedHotspots)
                }
            )
        }
    }
}

@Composable
fun HotspotBox(
    hotspot: HotSpot,
    onMove: (Offset) -> Unit,
    onResize: (Size, Offset) -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(hotspot.x.toFloat()) }
    var offsetY by remember { mutableStateOf(hotspot.y.toFloat()) }
    var width by remember { mutableStateOf(hotspot.width.toFloat()) }
    var height by remember { mutableStateOf(hotspot.height.toFloat()) }

    // Update the state when hotspot changes
    LaunchedEffect(hotspot) {
        offsetX = hotspot.x.toFloat()
        offsetY = hotspot.y.toFloat()
        width = hotspot.width.toFloat()
        height = hotspot.height.toFloat()
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .size(width.dp, height.dp)
            .border(2.dp, Color.Red, shape = RectangleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                    onMove(Offset(offsetX, offsetY))
                }
            }
    ) {
        // Delete button in top-right corner
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .padding(2.dp)
                .background(Color.Red, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete Hotspot",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Top-left resize handle
        ResizeHandle(
            modifier = Modifier
                .align(Alignment.TopStart)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newWidth = width - dragAmount.x
                        val newHeight = height - dragAmount.y
                        val newOffsetX = offsetX + dragAmount.x
                        val newOffsetY = offsetY + dragAmount.y
                        
                        if (newWidth >= 50 && newHeight >= 50) {
                            width = newWidth
                            height = newHeight
                            offsetX = newOffsetX
                            offsetY = newOffsetY
                            onResize(Size(width, height), Offset(offsetX, offsetY))
                        }
                    }
                }
        )

        // Bottom-left resize handle
        ResizeHandle(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newWidth = width - dragAmount.x
                        val newHeight = height + dragAmount.y
                        val newOffsetX = offsetX + dragAmount.x
                        
                        if (newWidth >= 50 && newHeight >= 50) {
                            width = newWidth
                            height = newHeight
                            offsetX = newOffsetX
                            onResize(Size(width, height), Offset(offsetX, offsetY))
                        }
                    }
                }
        )

        // Bottom-right resize handle
        ResizeHandle(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newWidth = width + dragAmount.x
                        val newHeight = height + dragAmount.y
                        
                        if (newWidth >= 50 && newHeight >= 50) {
                            width = newWidth
                            height = newHeight
                            onResize(Size(width, height), Offset(offsetX, offsetY))
                        }
                    }
                }
        )
    }
}

@Composable
fun ResizeHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .padding(4.dp)
            .background(Color.Blue.copy(alpha = 0.7f), CircleShape)
    )
}
