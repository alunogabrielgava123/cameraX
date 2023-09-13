import android.Manifest
import android.net.Uri
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.camerax.CameraView
import com.example.camerax.PermissionBox
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.File
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionContainer(
    outputDirectory: File,
    executor: Executor,
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        CameraScream(outputDirectory = outputDirectory, executor = executor)
    } else {
            PermissionBox(onClick = {
                cameraPermissionState.launchPermissionRequest()
            },
                description = "Esse app precisa da camera para funcionar",
                titlePermission = "Conceder permissão")
    }
}

