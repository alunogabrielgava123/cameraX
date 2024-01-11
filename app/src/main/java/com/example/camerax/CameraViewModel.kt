package com.example.camerax

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.IOException
import java.io.File
import java.io.FileOutputStream

class CameraViewModel : ViewModel() {

    val initState = CameraViewUiState(
        null,
        isFront = CameraSelector.DEFAULT_BACK_CAMERA,
        isFlash = false,
        error = null
    )

    val initPreviewImage = PreviewImage(
        uriImage = null,
        typeModel = TypeModel.CLASSIFICATION,
        classification =  Classification(score = listOf(Choice(nome = "Cat", rating = 0.92f),
            Choice(nome = "Dog", rating = 0.80f),  Choice(nome = "Turtle", rating = 0.20f)), modelo = "yollo-v8")
    )


    private val _uiState = MutableStateFlow<CameraViewUiState>(initState)
    val uiState : StateFlow<CameraViewUiState> = _uiState.asStateFlow()

    private val _previewImage = MutableStateFlow<PreviewImage>(initPreviewImage)
    val previewImage : StateFlow<PreviewImage> = _previewImage.asStateFlow()

    private  val _modelsResponse = MutableStateFlow<ModelsResponse>(ModelsResponse.Loading)
    val modelsResponse : StateFlow<ModelsResponse> = _modelsResponse.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    //Aplicar na hora de salvar a imagem de self
    fun flipHorizontal(bitmap: Bitmap): Bitmap {
        //flip horizontal image
        val matrix = Matrix()
        matrix.preScale(-1f, 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    init {
        getModels()
    }



    private  fun getModels() = viewModelScope.launch {
        try {
            val listModels = HttpService.api.getModels()
            _modelsResponse.value = ModelsResponse.Successes(listModels.models)
        } catch (e : IOException) {
            _modelsResponse.value = ModelsResponse.Error
            Log.e("CameraX", "Erro ${e.printStackTrace()}", e)
        }

    }

    private  fun bodyMultiPart(file : File): MultipartBody.Part {
        val requestFile = RequestBody.create("image/png".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("imagem", file.name, requestFile)
        return body
    }


    fun sendAndCalculateImage(context : Context, uri : Uri, scale: Float) = viewModelScope.launch {
        _isLoading.value = true
        try {
            //check image is black or white
            checkBlackOrWhite(context, uri)
            val file = imageUriToPng(context, uri)
            val body = bodyMultiPart(file)

            //TODO DO


        }  catch (error : Error) {
            Log.e("CameraViewModel", "Erro de compressao de imagem: ${error.message}", error)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun imageUriToPng(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val file = File(context.filesDir, "imagem.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()

        return file
    }





    private suspend  fun checkBlackOrWhite(context: Context, uri: Uri?) = withContext(Dispatchers.Default) {
        try {
            if (uri == null) {
                checkAndUpdateWhiteOurBlack(ErrorBackOrWhite("Sem imagem", typeError = TypeErrorIsBlackOurWhite.NOIMAGE))
                return@withContext
            }

            val isBlackOrWhite = isImageAllBlack(context, uri)
            val isWhite = isImageAllWhite(context, uri)
            if (isBlackOrWhite || isWhite) {
                checkAndUpdateWhiteOurBlack(ErrorBackOrWhite(message = "Imagem não pode ser detectada", typeError = TypeErrorIsBlackOurWhite.ERROIMAGE))
            } else {
                checkAndUpdateWhiteOurBlack(ErrorBackOrWhite(message = "Imagem enviada com sucesso", typeError = TypeErrorIsBlackOurWhite.IMAGEOK))
            }
        } catch (exception: Exception) {
            Log.e("CameraViewModel", "Erro ao verificar brilho da imagem: ${exception.message}", exception)
            // Atualizar o estado de erro, se necessário
            checkAndUpdateWhiteOurBlack( ErrorBackOrWhite("Error inesperado na hora de verificar a imagem", typeError = TypeErrorIsBlackOurWhite.ERROIMAGE)  )
        }
    }



    //Retornando ao estado de type model start
    private  fun changeStartState( typeModel : TypeModel ) {
        _previewImage.update {
            it -> it.copy(
                typeModel = typeModel
            )
        }
    }


    private fun saveBitmapToTempUri(context: Context, bitmap: Bitmap): Uri {
        val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
        }
        return Uri.fromFile(tempFile)
    }

    private fun isImageAllWhite(context: Context, imageUri: Uri): Boolean {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }

        val copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        var whitePixels = 0
        for (y in 0 until copyBitmap.height) {
            for (x in 0 until copyBitmap.width) {
                val pixel = copyBitmap.getPixel(x, y)
                val luminance = calculateLuminance(pixel)
                if (luminance > 245) { // Limiar para branco
                    whitePixels++
                }
            }
        }

        val totalPixels = copyBitmap.width * copyBitmap.height
        copyBitmap.recycle()

        // Verifica se todos os pixels são brancos
        return whitePixels == totalPixels
    }

    private fun isImageAllBlack(context: Context, imageUri: Uri): Boolean {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }

        val copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        var blackPixels = 0
        for (y in 0 until copyBitmap.height) {
            for (x in 0 until copyBitmap.width) {
                val pixel = copyBitmap.getPixel(x, y)
                val luminance = calculateLuminance(pixel)
                if (luminance < 0.5) { // Limiar para preto
                    blackPixels++
                }
            }
        }

        val totalPixels = copyBitmap.width * copyBitmap.height
        copyBitmap.recycle()

        // Verifica se todos os pixels são pretos
        return blackPixels == totalPixels
    }



    private fun calculateLuminance(color: Int): Double {
        val red = (color shr 16 and 0xff) / 255.0
        val green = (color shr 8 and 0xff) / 255.0
        val blue = (color and 0xff) / 255.0

        // Fórmula de luminância
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue
    }


    fun handlerEventCameraView(event : EventUi) {
        when(event) {
            is EventUi.TirandoFoto -> saveUiState(event.uri)
               EventUi.DeletandoFoto -> deleteImage()
            is  EventUi.TrocandoCamera -> setCamera()
               EventUi.AtivandoFlesh -> setFlash(value = !_uiState.value.isFlash)
            is EventUi.ChangeStart -> changeStartState(event.typeModel)
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

    private  fun checkAndUpdateWhiteOurBlack( error : ErrorBackOrWhite ) {
        _uiState.update { currentState -> currentState.copy(error = error) }
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



    //save ui State;
    private fun saveUiState(uri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(
                uri = uri,
                error = null
            )
        }
    }


    //deletando imagem;
    private  fun deleteImage() {
        _uiState.update {
            current ->  current.copy(
                uri = null,
                error = null
            )
        }
    }
}


sealed interface ModelsResponse {
    data class Successes(val models :  List<String>  ) : ModelsResponse
    object Error : ModelsResponse
    object  Loading : ModelsResponse
}


data class CameraViewUiState(
    val uri: Uri?,
    val isFront: CameraSelector,
    val isFlash: Boolean,
    val error : ErrorBackOrWhite?
)

//adicionar depois mais tipos como segmentacao entre outros
data class PreviewImage(
    val typeModel : TypeModel,
    val classification: Classification?,
    val uriImage : Uri?
)

data class ErrorBackOrWhite(
    var message : String,
    var typeError : TypeErrorIsBlackOurWhite
)

enum class TypeErrorIsBlackOurWhite {
   ERROIMAGE, NOIMAGE, IMAGEOK
}

enum class TypeModel {
    CLASSIFICATION, DETECTION, SEGMENTATION, START
}

sealed class EventUi {
    data class TirandoFoto( val uri : Uri ) : EventUi()
    data class ChangeStart( val typeModel: TypeModel  ) : EventUi()
    object TrocandoCamera : EventUi()
    object AtivandoFlesh: EventUi()
    object DeletandoFoto :EventUi()
}






