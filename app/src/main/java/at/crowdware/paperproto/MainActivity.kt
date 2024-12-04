package at.crowdware.paperproto

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            var screenState by remember { mutableStateOf(ScreenState.PreviewMode) }
            val pages = remember { mutableStateListOf<Page>() }
            var currentPage by remember { mutableStateOf<Page?>(null) }
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                val loadedPages = loadPages(context)
                pages.addAll(loadedPages)
                currentPage = loadedPages.firstOrNull()
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surface, RoundedCornerShape(
                                    topEnd = 16.dp, bottomEnd = 16.dp
                                )
                            )
                            .padding(16.dp),
                        onOpenCamera = {
                            screenState = ScreenState.CameraView
                            scope.launch { drawerState.close() }
                        }, onHotSpotAdd = {
                            currentPage?.let { page ->
                                val pageIndex = pages.indexOfFirst { it.id == page.id }
                                if (pageIndex != -1) {
                                    pages[pageIndex].hotSpots.add(HotSpot(10, 10, 100, 100, ""))
                                    savePages(context, pages)
                                }
                            }
                        },
                        pages,
                        onDelete = { page -> deletePage(context, page, pages)},
                        onShowImage = { page ->
                            // Find the page in the pages list to ensure we're using the latest version
                            val pageIndex = pages.indexOfFirst { it.id == page.id }
                            if (pageIndex != -1) {
                                currentPage = pages[pageIndex]
                            }
                        }
                    )
                }
            ) {
                when (screenState) {
                    ScreenState.PreviewMode -> currentPage?.let { currentPageValue ->
                        PreviewModeScreen(
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            },  
                            page = currentPageValue,
                            onHotspotsChanged = { newHotspots ->
                                // Find and update the page in the pages list
                                val pageIndex = pages.indexOfFirst { it.id == currentPageValue.id }
                                if (pageIndex != -1) {
                                    // Update the hotspots in the pages list
                                    pages[pageIndex] = pages[pageIndex].copy(hotSpots = newHotspots.toMutableList())
                                    // Update currentPage to reflect the changes
                                    currentPage = pages[pageIndex]
                                    // Save changes to persistent storage immediately
                                    savePages(context, pages)
                                }
                            }
                        )
                    }

                    ScreenState.CameraView -> CameraView(
                        onImageCaptured = { uri ->
                            currentPage = addNewPage(context, uri.toString(), pages)
                            screenState = ScreenState.PreviewMode
                        },
                        onError = { exception ->
                            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun DrawerContent(
        modifier: Modifier = Modifier,
        onOpenCamera: () -> Unit,
        onHotSpotAdd: () -> Unit,
        pages: MutableList<Page>,
        onDelete: (Page) -> Unit,
        onShowImage: (Page) -> Unit
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "PaperProto", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(text = "(C) 2024 by CrowdWare", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(pages.size) { index ->
                    val page = pages[index]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                            .clickable { onShowImage(page) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Page name
                            Text(
                                text = page.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            // Delete button
                            IconButton(onClick = { onDelete(page) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Page"
                                )
                            }
                        }
                    }
                }
            }
            Button(onClick = onOpenCamera) {
                Text("Take Picture")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onHotSpotAdd) {
                Text("Add Hotspot")
            }
        }
    }

    @Composable
    fun PreviewModeScreen(
        onOpenDrawer: () -> Unit, 
        page: Page,
        onHotspotsChanged: (List<HotSpot>) -> Unit
    ) {
        Box(
            Modifier
                .fillMaxSize()
        ) {
            if (page != null) {
                ImageWithMultipleHotspots(
                    page = page,
                    onHotspotsChanged = onHotspotsChanged
                )
            } else {
                Column (modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally){
                    Text(text = "Welcome to PaperProto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "The idea behind this app is to take pictures from your paper prototypes and use them as an interactive mockup.\nIf you mark a hotspot you are able to simulate a button when in preview mode. Clicking a button opens another picture.\n\nSo first step will be to take a picture.\nTo take a picture open the drawer by swiping from the left to the right and click on 'Take Picture'.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

enum class ScreenState {
    PreviewMode,
    CameraView
}
