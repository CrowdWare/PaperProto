package at.crowdware.paperproto

import android.os.Bundle
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
            var imageUri by remember { mutableStateOf<Uri?>(null) }

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
                        },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                when (screenState) {
                    ScreenState.PreviewMode -> PreviewModeScreen(
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }, imageUri = imageUri
                    )

                    ScreenState.CameraView -> CameraView(
                        onImageCaptured = { uri ->
                            imageUri = uri
                            //Toast.makeText(this, "Image captured: $uri", Toast.LENGTH_SHORT).show()
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
        onCloseDrawer: () -> Unit
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "PaperProto", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpenCamera) {
                Text("Take Picture")
            }
            /*Spacer(Modifier.height(8.dp))
            Button(onClick = onCloseDrawer) {
                Text("Close Drawer")
            }*/
        }
    }

    @Composable
    fun PreviewModeScreen(onOpenDrawer: () -> Unit, imageUri: Uri?) {
        Box(
            Modifier
                .fillMaxSize()
                //.padding(16.dp)
        ) {
            if (imageUri != null) {
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column {
                    Text(text = "Welcome to PaperProto", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "The idea behind this app is to take pictures from your paper prototypes and use them as mockup.\nIf you mark a hotspot you are able to simulate a button when in preview mode. Clicking a button opens another picture.\n\nSo first step will be to take a picture.\nTo take a picture open the drawer by swiping from the left to the right and click on 'Take Picture'.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            //LaunchedEffect(Unit) {
            //    onOpenDrawer()
            //}
        }
    }
}

enum class ScreenState {
    PreviewMode,
    CameraView
}