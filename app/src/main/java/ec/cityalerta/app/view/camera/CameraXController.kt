package ec.cityalerta.app.view.camera

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import java.io.File

class CameraXController(
    private val context: Context
) : CameraController {
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    private var imageCapture: ImageCapture? = null
    private val internalPreviewView = PreviewView(context)
    private var currentLensFacing = CameraSelector.LENS_FACING_BACK
    private var currentLifecycleOwner: LifecycleOwner? = null

    override val previewView: View = internalPreviewView

    override fun bind(lifecycleOwner: LifecycleOwner) {
        currentLifecycleOwner = lifecycleOwner
        val executor = ContextCompat.getMainExecutor(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(internalPreviewView.surfaceProvider)
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(currentLensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    capture
                )
                imageCapture = capture
            } catch (e: Exception) {
                // Log error or handle failure
            }
        }, executor)
    }

    override fun switchCamera() {
        currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        currentLifecycleOwner?.let { bind(it) }
    }

    override fun capturePhoto(onSuccess: (Uri) -> Unit, onError: (String) -> Unit) {
        val capture = imageCapture
        if (capture == null) {
            onError("Camara no lista")
            return
        }

        val imageFile = File(context.cacheDir, "reporte_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()
        val executor = ContextCompat.getMainExecutor(context)

        capture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        imageFile
                    )
                    onSuccess(uri)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError("No se pudo capturar la foto")
                }
            }
        )
    }

    override fun release() {
        runCatching { cameraProviderFuture.get().unbindAll() }
    }
}
