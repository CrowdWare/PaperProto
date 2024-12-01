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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            var screenState by remember { mutableStateOf(ScreenState.PreviewMode) }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surface,  RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp
                            ))
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
                        }
                    )
                    ScreenState.CameraView -> CameraView(
                        onImageCaptured = { uri ->
                            Toast.makeText(this, "Image captured: $uri", Toast.LENGTH_SHORT).show()
                            screenState = ScreenState.PreviewMode
                        },
                        onError = { exception ->
                            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
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
        onCloseDrawer: () -> Unit) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "PaperProto", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpenCamera) {
                Text("Open Camera")
            }
            /*Spacer(Modifier.height(8.dp))
            Button(onClick = onCloseDrawer) {
                Text("Close Drawer")
            }*/
        }
    }

    @Composable
    fun PreviewModeScreen(onOpenDrawer: () -> Unit) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "You have not yet captured an image.", style = MaterialTheme.typography.bodyLarge)
            LaunchedEffect(Unit) {
                onOpenDrawer()
            }
        }
    }
}

enum class ScreenState {
    PreviewMode,
    CameraView
}