package com.example.camerax

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    val initState = CameraViewUiState(
        null,
        null,
        isFront = CameraSelector.DEFAULT_BACK_CAMERA,
        isFlash = false,
        listOf(
            Filtro(isSelct = true, filtroColor = null),
            Filtro(filtroColor = ColorFilter.colorMatrix(
                androidx.compose.ui.graphics.ColorMatrix().apply {
                    setToSaturation(1.5f)  // Aumenta a saturação
                }
            )),
            Filtro(filtroColor = ColorFilter.colorMatrix(
                androidx.compose.ui.graphics.ColorMatrix().apply {
                    setToSaturation(0.5f)  // Reduz a saturação
                }
            )),
            Filtro(filtroColor = ColorFilter.colorMatrix(
                androidx.compose.ui.graphics.ColorMatrix().apply {
                    sepiaMatrix()  // Efeito sepia
                }
            )),
            Filtro(filtroColor = ColorFilter.colorMatrix(
                androidx.compose.ui.graphics.ColorMatrix().apply {
                    sepiaMatrix()  // Efeito sepia
                }
            ))
        ),
    )

    private val _uiState = MutableStateFlow<CameraViewUiState>(initState)
    val uiState : StateFlow<CameraViewUiState> = _uiState.asStateFlow()


    fun saveImageMidiaStorage(title: String, context: Context , scaleFactor: Float) {
        viewModelScope.launch {
            try {

                if(_uiState.value.uri != null) {
                    val bitmap = getBitmapFromUri(_uiState.value.uri!!, context)!!

                    //filtrando o elemente que esta selecionado
                    val filtro : Filtro? = _uiState.value.filtros.find { it.isSelct }

                    if(_uiState.value.isFront == CameraSelector.DEFAULT_FRONT_CAMERA) {
                        val dessespelhada = flipHorizontal(bitmap)
                        Log.i("filtro", "${filtro}")
                        saveBitmapToGallery(context, dessespelhada, title)
                    } else {
                        Log.i("filtro", "${filtro}")
                        saveBitmapToGallery(context, bitmap, title)
                    }
                }
                //emitindo o evento para salvar a imagem no storage do dispositivo

            }catch (e : Error) {
                //emitindo o evento para salvar a imagem no storago porem o erro
                Log.i("kilo", "Erro ao salvar imagem ${e.message}")
            }
        }
    }

//    fun filterBitmap(input: Bitmap, composeFilter: ColorFilter?): Bitmap {
//        val output = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(output)
//        val paint = Paint().apply {
//            colorFilter = composeFilter?.asAndroidColorFilter()
//        }
//        canvas.drawBitmap(input, 0f, 0f, paint)
//        return output
//    }




    fun flipHorizontal(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.preScale(-1f, 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String) {

        viewModelScope.launch(Dispatchers.IO) {

            try {
                // código restante...
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$title.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                }

                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    context.contentResolver.openOutputStream(uri)?.let { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()
                    }
                }

            }catch (e:Error) {
                Log.i("kilo", "All permissions granted")
            }

        }
    }


    fun handlerEventCameraView(event : EventUi) {
        when(event) {
            is EventUi.TirandoFoto -> saveUiState(event.uri)
               EventUi.DeletandoFoto -> deleteImage()
            is  EventUi.TrocandoCamera -> setCamera()
               EventUi.AtivandoFlesh -> setFlash(value = !_uiState.value.isFlash)

        }
    }



    
    private fun setCamera() {

        if(uiState.value.isFront == CameraSelector.DEFAULT_FRONT_CAMERA) {
            _uiState.update {
                    currentValeu -> currentValeu.copy(
                isFront = CameraSelector.DEFAULT_BACK_CAMERA
            )
            }
        } else {
            _uiState.update {
                    currentValeu -> currentValeu.copy(
                isFront = CameraSelector.DEFAULT_FRONT_CAMERA
            )
            }
        }
    }

    private fun setFlash(value : Boolean) {
        _uiState.update {
                currentValeu -> currentValeu.copy(
            isFlash = value
        )
        }
    }

    private suspend  fun getBitmapFromUri(uri: Uri, context: Context): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }





    fun selectFiltro(selectedFiltro: Filtro) {
        _uiState.update { currentState ->
            val updatedFiltros = currentState.filtros.map { filtro ->
                if (filtro == selectedFiltro) {
                    filtro.copy(isSelct = true)
                } else {
                    filtro.copy(isSelct = false)
                }
            }

            currentState.copy(
                filtro = selectedFiltro,
                filtros = updatedFiltros
            )
        }
    }

    

    //save ui State;
    private fun saveUiState(uri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(
                uri = uri,
                filtro = null
            )
        }
    }


    //deletando imagem;
    private  fun deleteImage() {
        _uiState.update {
            current ->  current.copy(
                uri = null,
                filtros = initState.filtros,
                filtro = null
            )
        }
    }

}





data class CameraViewUiState(
    val uri: Uri?,
    val filtro: Filtro?,
    val isFront: CameraSelector,
    val isFlash: Boolean,
    val filtros: List<Filtro>
)


data class Filtro(
    val isSelct : Boolean = false,
    val filtroColor : ColorFilter?
)




sealed class EventUi {
    data class TirandoFoto( val uri : Uri ) : EventUi()

    object TrocandoCamera : EventUi()
    object AtivandoFlesh: EventUi()
    object DeletandoFoto :EventUi()
}


fun sepiaMatrix(): androidx.compose.ui.graphics.ColorMatrix {
    val matrix = androidx.compose.ui.graphics.ColorMatrix()
    matrix[0, 0] = 0.393f
    matrix[0, 1] = 0.769f
    matrix[0, 2] = 0.189f
    matrix[1, 0] = 0.349f
    matrix[1, 1] = 0.686f
    matrix[1, 2] = 0.168f
    matrix[2, 0] = 0.272f
    matrix[2, 1] = 0.534f
    matrix[2, 2] = 0.131f
    return matrix
}

fun invertMatrix(): androidx.compose.ui.graphics.ColorMatrix {
    val matrix = androidx.compose.ui.graphics.ColorMatrix()
    matrix[0, 0] = -1f
    matrix[0, 3] = 255f
    matrix[1, 1] = -1f
    matrix[1, 3] = 255f
    matrix[2, 2] = -1f
    matrix[2, 3] = 255f
    return matrix
}

fun posterizeMatrix(steps: Float): androidx.compose.ui.graphics.ColorMatrix {
    val scale = 255f / steps
    val translation = 255f / (steps - 1)
    val matrix = androidx.compose.ui.graphics.ColorMatrix()
    matrix[0, 0] = scale
    matrix[0, 3] = translation
    matrix[1, 1] = scale
    matrix[1, 3] = translation
    matrix[2, 2] = scale
    matrix[2, 3] = translation
    return matrix
}

fun grayscaleMatrix(): androidx.compose.ui.graphics.ColorMatrix {
    val matrix = androidx.compose.ui.graphics.ColorMatrix()
    matrix[0, 0] = 0.2126f
    matrix[0, 1] = 0.7152f
    matrix[0, 2] = 0.0722f
    matrix[1, 0] = 0.2126f
    matrix[1, 1] = 0.7152f
    matrix[1, 2] = 0.0722f
    matrix[2, 0] = 0.2126f
    matrix[2, 1] = 0.7152f
    matrix[2, 2] = 0.0722f
    return matrix
}




