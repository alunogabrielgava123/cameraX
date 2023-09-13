import android.net.Uri
import androidx.camera.core.ImageCaptureException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.camerax.CameraView
import com.example.camerax.CameraViewModel
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CameraScream(
    outputDirectory: File,
    executor: Executor,
) {
    val cameraViewModel : CameraViewModel = viewModel()
    val uiState = cameraViewModel.uiState.collectAsState().value

    if(uiState.uri != null) {
        ImagePreview()
    } else {
        CameraView(outputDirectory =outputDirectory ,executor = executor)
    }


}