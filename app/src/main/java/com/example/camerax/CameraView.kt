package com.example.camerax
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Rational
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.UseCase
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.sharp.FlipCameraAndroid
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.io.File
import java.util.concurrent.Executor
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.FileOutputStream

@androidx.camera.core.ExperimentalUseCaseGroup
@SuppressLint("RestrictedApi", "Range")
@Composable
fun  CameraView(
    outputDirectory: File,
    executor: Executor,
    viewModelCameraView : CameraViewModel = viewModel(),
) {

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    DisposableEffect(systemUiController, useDarkIcons) {
        // Update all of the system bar colors to be transparent, and use
        // dark icons if we're in light theme
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = false
        )

        // setStatusBarColor() and setNavigationBarColor() also exist

        onDispose {}
    }


    val uiState = viewModelCameraView.uiState.collectAsState().value
    val (isSelect, setSelect) = remember {
        mutableStateOf<Boolean>(false)
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = androidx.camera.core.Preview.Builder().build()

    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }

    var scaleFactor by remember { mutableStateOf(1f) }
    var lastScaleFactor by remember { mutableStateOf(0f) }

    val previewView = remember { PreviewView(context)}


    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(uiState.isFront.lensFacing!!)
        .build()


    LaunchedEffect(uiState.isFront) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()

        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)

    }

    LaunchedEffect(uiState.isFlash) {
        if (uiState.isFlash) {
            imageCapture.flashMode = ImageCapture.FLASH_MODE_ON
        } else {
            imageCapture.flashMode = ImageCapture.FLASH_MODE_OFF
        }
    }



    LaunchedEffect(uiState.isFront) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )


        //previewView.controller.cameraControl = camera

        preview.setSurfaceProvider(previewView.surfaceProvider)


    }




    val zoomGestureModifier = Modifier.pointerInput(Unit) {
        val velocityTracker = VelocityTracker()
        detectTransformGestures { _, pan, zoom, _ ->
            scaleFactor *= zoom
            val camera = imageCapture.camera
            val currentZoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
            val newZoomRatio = currentZoomRatio * scaleFactor / lastScaleFactor
            lastScaleFactor = scaleFactor
            camera?.cameraControl?.setZoomRatio(newZoomRatio)
        }
    }




    Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 1f)
                .background(Color.Black)
                .padding(2.dp)
                .clip(RoundedCornerShape(32.dp, 32.dp, 32.dp, 32.dp))
                .then(zoomGestureModifier)
    )
        {

            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

            IconButton(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(80.dp),
                onClick = { viewModelCameraView.handlerEventCameraView(EventUi.AtivandoFlesh) }
            ) {
                if(uiState.isFlash) {
                    Icon(

                        imageVector = Icons.Default.FlashOn, // exemplo de ícone
                        contentDescription = "Toggle Flash",
                        tint = Color.White ,
                        modifier = Modifier
                            .size(50.dp)
                            .padding(1.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FlashOff, // exemplo de ícone
                        contentDescription = "Toggle Flash",
                        tint = Color.White,
                        modifier = Modifier
                            .size(50.dp)
                            .padding(1.dp)
                    )
                }
            }

            IconButton(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(32.dp),
                onClick = {
                    setSelect(!isSelect)
                    viewModelCameraView.handlerEventCameraView(EventUi.TrocandoCamera)
                },

                ) {
                Icon(
                    imageVector = Icons.Sharp.FlipCameraAndroid,
                    contentDescription = "Take picture",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(1.dp)

                )
            }

            IconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .size(80.dp),
                onClick = {
                    takePhoto(
                    filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                    imageCapture = imageCapture,
                    outputDirectory = outputDirectory,
                    executor = executor,
                    onError = {},
                    onImageCaptured = {
                        uri -> viewModelCameraView.handlerEventCameraView(EventUi.TirandoFoto(uri = uri))
                    }
                )
                },

                ) {
                Icon(
                    imageVector = Icons.Sharp.Lens,
                    contentDescription = "Take picture",
                    tint = Color.White,
                    modifier = Modifier
                        .size(260.dp)
                        .padding(1.dp)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }
}

fun getBitmapFromUri(uri: Uri, context: Context): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}

fun scaleBitmap(original: Bitmap, scaleFactor: Float): Bitmap {
    val width = (original.width * scaleFactor).toInt()
    val height = (original.height * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(original, width, height, true)
}

fun saveBitmapToFile(bitmap: Bitmap, outputFile: File) {
    val outputStream = FileOutputStream(outputFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    outputStream.flush()
    outputStream.close()
}

fun saveZoomedImage(uri: Uri, scaleFactor: Float, context: Context, outputFile: File) {
    val originalBitmap = getBitmapFromUri(uri, context)
    val zoomedBitmap = originalBitmap?.let { scaleBitmap(it, scaleFactor) }
    if (zoomedBitmap != null) {
        saveBitmapToFile(zoomedBitmap, outputFile)
    }
}




