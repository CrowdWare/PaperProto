package at.crowdware.paperproto

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    allPages: List<Page>,
    isEditMode: Boolean,
    onHotspotsChanged: (List<HotSpot>) -> Unit,
    onHotspotClicked: (String) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberImagePainter(Uri.parse(page.picture)),
            contentDescription = "Image with hotspots",
            modifier = Modifier.fillMaxSize()
        )

        page.hotSpots.forEachIndexed { index, hotspot ->
            if (isEditMode) {
                EditableHotspotBox(
                    hotspot = hotspot,
                    allPages = allPages,
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
                                width = newSize.width.toInt(),
                                height = newSize.height.toInt()
                            )
                        }
                        onHotspotsChanged(updatedHotspots)
                    },
                    onDelete = {
                        val updatedHotspots = page.hotSpots.toMutableList().apply {
                            removeAt(index)
                        }
                        onHotspotsChanged(updatedHotspots)
                    },
                    onLinkChanged = { newLink ->
                        val updatedHotspots = page.hotSpots.toMutableList().apply {
                            this[index] = this[index].copy(link = newLink)
                        }
                        onHotspotsChanged(updatedHotspots)
                    }
                )
            } else {
                ClickableHotspotBox(
                    hotspot = hotspot,
                    onClick = { onHotspotClicked(hotspot.link) }
                )
            }
        }
    }
}

@Composable
fun EditableHotspotBox(
    hotspot: HotSpot,
    allPages: List<Page>,
    onMove: (Offset) -> Unit,
    onResize: (Size, Offset) -> Unit,
    onDelete: () -> Unit,
    onLinkChanged: (String) -> Unit
) {
    var offsetX by remember { mutableStateOf(hotspot.x.toFloat()) }
    var offsetY by remember { mutableStateOf(hotspot.y.toFloat()) }
    var width by remember { mutableStateOf(hotspot.width.toFloat()) }
    var height by remember { mutableStateOf(hotspot.height.toFloat()) }
    var showLinkDialog by remember { mutableStateOf(false) }

    // Update the state when hotspot changes
    LaunchedEffect(hotspot) {
        offsetX = hotspot.x.toFloat()
        offsetY = hotspot.y.toFloat()
        width = hotspot.width.toFloat()
        height = hotspot.height.toFloat()
    }

    if (showLinkDialog) {
        LinkDialog(
            currentLink = hotspot.link,
            allPages = allPages,
            onDismiss = { showLinkDialog = false },
            onLinkSelected = { 
                onLinkChanged(it)
                showLinkDialog = false
            }
        )
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

        // Link button in top-left corner
        IconButton(
            onClick = { showLinkDialog = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(24.dp)
                .padding(2.dp)
                .background(if (hotspot.link.isNotEmpty()) Color.Green else Color.Gray, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Set Link",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Left resize handle
        var leftHandleStartX by remember { mutableStateOf(0f) }
        var rightEdgeStartX by remember { mutableStateOf(0f) }
        
        ResizeHandle(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            leftHandleStartX = offsetX
                            rightEdgeStartX = offsetX + width
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        // Move the left edge with the finger
                        val newX = offsetX + dragAmount.x
                        // Calculate new width based on distance to original right edge
                        val newWidth = rightEdgeStartX - newX
                        val newHeight = height + dragAmount.y
                        
                        if (newWidth >= 50 && newHeight >= 50) {
                            offsetX = newX
                            width = newWidth
                            height = newHeight
                            onResize(Size(width, height), Offset(offsetX, offsetY))
                        }
                    }
                }
        )

        // Right resize handle
        var leftEdgeStartX by remember { mutableStateOf(0f) }
        
        ResizeHandle(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            leftEdgeStartX = offsetX
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        // Calculate new width based on handle position relative to left edge
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
fun ClickableHotspotBox(
    hotspot: HotSpot,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(hotspot.x, hotspot.y) }
            .size(hotspot.width.dp, hotspot.height.dp)
            .background(
                color = if (hotspot.link.isNotEmpty()) 
                    Color(0x33007AFF) // Semi-transparent blue for linked hotspots
                else 
                    Color(0x33FF0000) // Semi-transparent red for unlinked hotspots
            )
            .clickable(enabled = hotspot.link.isNotEmpty()) { onClick() }
    )
}

@Composable
fun LinkDialog(
    currentLink: String,
    allPages: List<Page>,
    onDismiss: () -> Unit,
    onLinkSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Target Page") },
        text = {
            Column {
                Text("Select the page this hotspot should link to:")
                Spacer(modifier = Modifier.height(8.dp))
                allPages.forEach { page ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLinkSelected(page.id.toString()) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = page.name,
                            color = if (page.id.toString() == currentLink) Color.Blue else Color.Black
                        )
                        if (page.id.toString() == currentLink) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("(Selected)", color = Color.Blue)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
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
