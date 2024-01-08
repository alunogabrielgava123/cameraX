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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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


@Composable
fun ImagePreview(
    cameraViewModel : CameraViewModel = viewModel()
) {

    val uiState = cameraViewModel.uiState.collectAsState().value
    var expanded by remember { mutableStateOf(false) }


    val context = LocalContext.current

    val scale = remember { mutableStateOf(0.92f) }
    val rotationState = remember { mutableStateOf(0.92f) }



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
                    .padding(3.dp)
                    .fillMaxHeight(fraction = 0.92f)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer(scaleX = -1f)
            } else {
                Modifier
                    .padding(3.dp)
                    .fillMaxHeight(fraction = 0.92f)
                    .align(Alignment.BottomCenter)
            }

               Image(
                   painter = rememberImagePainter(uiState.uri),
                   contentDescription = null,
                   modifier = modifer,
                   colorFilter = uiState.filtro?.filtroColor,
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
                                    cameraViewModel.saveImageMidiaStorage("imagem", context = context, 10f)
                                    expanded = false
                                    Toast.makeText(context, "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show()
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
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = 0.08f)
                ) {
                    Row( modifier = Modifier.align(Alignment.BottomEnd)  ) {
                        IconButton( onClick = { /*TODO*/ },) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Sharp.Send,
                                contentDescription = "Take picture",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(32.dp)

                            )
                        }
                    }
            }

        }
    }
}
