@file:Suppress("IMPLICIT_CAST_TO_ANY")

import android.content.Context
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.sharp.ArrowBackIosNew
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.MoreHoriz
import androidx.compose.material.icons.sharp.SaveAlt
import androidx.compose.material.icons.sharp.Send
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.camerax.CameraViewModel
import com.example.camerax.EventUi
import com.example.camerax.TypeErrorIsBlackOurWhite


@Composable
fun ImagePreview(
    cameraViewModel : CameraViewModel = viewModel()
) {

    val uiState = cameraViewModel.uiState.collectAsState().value
    val isLoading = cameraViewModel.isLoading.collectAsState().value
    var expanded by remember { mutableStateOf(false) }


    val context = LocalContext.current

    var scale by remember { mutableStateOf(1f) }




    Column {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 1f)
                    .background(Color.Black)
                    .padding(0.dp)

            ) {



            val modifer = if(uiState.isFront == CameraSelector.DEFAULT_FRONT_CAMERA) {
                Modifier
                    .padding(0.dp)
                    .fillMaxHeight(fraction = 1f)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer(
                        // Aplica o zoom
                        scaleX = maxOf(1f, scale),
                        scaleY = maxOf(1f, scale)
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale *= zoom
                        }
                    }
            } else {
                Modifier
                    .padding(0.dp)
                    .fillMaxHeight(fraction = 1f)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer(
                        // Aplica o zoom
                        scaleX = maxOf(1f, scale),
                        scaleY = maxOf(1f, scale)
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale *= zoom
                        }
                    }
            }

               Image(
                   painter = rememberImagePainter(uiState.uri),
                   contentDescription = null,
                   modifier = modifer,
                   contentScale = ContentScale.Crop,

               )

                IconButton(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    onClick = {
                        cameraViewModel.handlerEventCameraView(EventUi.DeletandoFoto)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.DarkGray, CircleShape)
                            .padding(4.dp)
                    ){
                        Icon(
                            imageVector = Icons.Sharp.ArrowBackIosNew,
                            contentDescription = "Take picture",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)

                        )
                    }
                }

                IconButton(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    onClick = {
                        expanded = true
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.DarkGray, CircleShape)
                            .padding(4.dp)
                    ){

                        Icon(
                            imageVector = Icons.Sharp.MoreHoriz,
                            contentDescription = "Take picture",
                            tint = Color.White,
                            modifier = Modifier
                                .size(32.dp)

                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Salvar") },
                                onClick = {
                                  expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.SaveAlt,
                                        contentDescription = null
                                    )
                                })
                            DropdownMenuItem(
                                text = { Text("Desenhar") },
                                onClick = {
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Draw,
                                        contentDescription = null
                                    )
                                })
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .background(Color.Black)
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 8.dp)
                        .fillMaxHeight(fraction = 0.08f)
                ) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                                .fillMaxHeight()
                        ) {

                            if(isLoading) {
                                Box(  modifier = Modifier.height(30.dp).width(30.dp).align(Alignment.CenterStart) ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                if(uiState.error == null) {
                                    Text("Estou esperando voce aqui", color = Color.White, modifier = Modifier.align(Alignment.CenterStart))
                                } else {
                                    Text(uiState.error.message, color = Color.White, modifier = Modifier.align(Alignment.CenterStart))
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = 8.dp)

                        ) {
                            IconButton(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                onClick = {
                                    uiState.uri?.let {
                                        cameraViewModel.sendAndCalculateImage(context, it, scale)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Sharp.Send,
                                    contentDescription = "Enviar",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
        }
    }
}
