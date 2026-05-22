package ec.cityalerta.app.view.camera

import android.net.Uri
import android.view.View
import androidx.lifecycle.LifecycleOwner

interface CameraController {
    val previewView: View

    fun bind(lifecycleOwner: LifecycleOwner)

    fun capturePhoto(
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    )

    fun release()
}
