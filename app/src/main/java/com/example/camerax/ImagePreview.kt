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
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
                    .fillMaxHeight(fraction = 0.92f)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer(scaleX = -1f)
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, rotation ->
                            scale.value *= zoom
                            rotationState.value += rotation
                        }
                    }
                    .graphicsLayer(
                        // adding some zoom limits (min 50%, max 200%)
                        scaleX = maxOf(.5f, minOf(3f, scale.value)),
                        scaleY = maxOf(.5f, minOf(3f, scale.value)),
                        rotationZ = rotationState.value
                    )
            } else {
                Modifier
                    .fillMaxHeight(fraction = 0.92f)
                    .align(Alignment.BottomCenter)
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, rotation ->
                            scale.value *= zoom
                            rotationState.value += rotation
                        }
                    }
                    .graphicsLayer(
                        // adding some zoom limits (min 50%, max 200%)
                        scaleX = maxOf(.5f, minOf(3f, scale.value)),
                        scaleY = maxOf(.5f, minOf(3f, scale.value)),
                        rotationZ = rotationState.value
                    )
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


                Row(
                    modifier = Modifier
                        .background(Color.Black)
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = 0.15f)
                ) {
                    val scrollState = rememberScrollState()

                    Row(
                        modifier = Modifier
                            .horizontalScroll(scrollState)
                    ) {
                        // Adicionando várias imagens na linha
                        repeat(uiState.filtros.size) { index ->  // Supondo que você tenha 10 imagens

                            val filtro = uiState.filtros[index]

                            val modifier = if(uiState.isFront == CameraSelector.DEFAULT_FRONT_CAMERA)  {
                                val modifier = Modifier// Recorte arredondado para a imagem
                                    .fillMaxSize()
                                    .size(100.dp, 100.dp)
                                    .graphicsLayer(scaleX = -1f)
                                    .clickable {
                                        cameraViewModel.selectFiltro(filtro)
                                    }
                                    .padding(8.dp)
                                    .border(
                                        1.dp, if (filtro.isSelct) {
                                            Color.Gray
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                modifier
                            } else {
                                val modifier = Modifier// Recorte arredondado para a imagem
                                    .fillMaxSize()
                                    .size(100.dp, 100.dp)
                                    .clickable {
                                        cameraViewModel.selectFiltro(filtro)
                                    }
                                    .padding(8.dp)
                                    .border(
                                        1.dp, if (filtro.isSelct) {
                                            Color.Gray
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                modifier
                            }

                            Image(
                                contentScale = ContentScale.Crop,
                                painter = rememberImagePainter(uiState.uri),
                                contentDescription = null,
                                colorFilter = filtro.filtroColor,
                                modifier = modifier)
                        }
                    }


            }

        }
    }
}
