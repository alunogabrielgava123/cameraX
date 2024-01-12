import android.net.Uri
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.camerax.CameraView
import com.example.camerax.CameraViewModel
import java.io.File
import java.util.concurrent.Executor

@Composable
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalUseCaseGroup::class)
fun  CameraScream(
    outputDirectory: File,
    executor: Executor,
) {
    val cameraViewModel : CameraViewModel = viewModel()
    val previewImage = cameraViewModel.previewImage.collectAsState().value

    if(previewImage.uriImage != null) {
       ImagePreview()
    } else {
        CameraView(outputDirectory =outputDirectory ,executor = executor)
    }
}