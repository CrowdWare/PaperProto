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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
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

@Composable
fun ImageWithMultipleHotspots(
    page: Page,
    onHotspotsChanged: (List<HotSpot>) -> Unit) {
    var modifiedHotspots by remember(page) { mutableStateOf(page.hotSpots.toMutableList()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberImagePainter(Uri.parse(page.picture)),
            contentDescription = "Image with hotspots",
            modifier = Modifier.fillMaxSize()
        )

        modifiedHotspots.forEachIndexed { index, hotspot ->
            HotspotBox(
                hotspot = hotspot,
                onMove = { newPosition ->
                    println("New position: $newPosition")
                    // Ersetze die Liste mit einer aktualisierten Kopie
                    modifiedHotspots = modifiedHotspots.toMutableList().apply {
                        this[index] = this[index].copy(
                            x = newPosition.x.toInt(),
                            y = newPosition.y.toInt()
                        )
                    }
                    onHotspotsChanged(modifiedHotspots.toList())
                },
                onResize = { newSize ->
                    modifiedHotspots = modifiedHotspots.toMutableList().apply {
                        this[index] = this[index].copy(
                            width = newSize.width.toInt(),
                            height = newSize.height.toInt()
                        )
                    }
                    onHotspotsChanged(modifiedHotspots.toList())
                }
            )
        }
    }
}
@Composable
fun HotspotBox(
    hotspot: HotSpot,
    onMove: (Offset) -> Unit,
    onResize: (Size) -> Unit
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(hotspot.x, hotspot.y) }
            .size(hotspot.width.dp, hotspot.height.dp)
            .border(2.dp, Color.Red, shape = RectangleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    onMove(Offset(hotspot.x + dragAmount.x, hotspot.y + dragAmount.y))
                    change.consume()
                }
            }
    ) {
        // Resize handles (bottom-right)
        Handle(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, _, _ ->
                        val newSize = Size(hotspot.width + pan.x, hotspot.height + pan.y)
                        onResize(newSize)
                    }
                }
        )
    }
}

@Composable
fun Handle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.Gray, CircleShape)
            .pointerInput(Unit) {}
    )
}