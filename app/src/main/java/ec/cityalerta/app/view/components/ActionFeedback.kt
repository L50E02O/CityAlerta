package ec.cityalerta.app.view.components

import android.util.Log
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Componente desacoplado para manejar mensajes de feedback (información y errores)
 * a través de un SnackbarHostState.
 */
@Composable
fun ActionFeedbackHandler(
    infoMessage: String?,
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    onDismissInfo: () -> Unit,
    onDismissError: () -> Unit = {}
) {
    // Escucha mensajes de información/éxito
    LaunchedEffect(infoMessage) {
        Log.d("ActionFeedbackHandler", "infoMessage recibido: $infoMessage")
        infoMessage?.let {
            Log.d("ActionFeedbackHandler", "Mostrando snackbar: $it")
            snackbarHostState.showSnackbar(message = it)
            Log.d("ActionFeedbackHandler", "Snackbar mostrado, llamando a onDismissInfo")
            onDismissInfo()
        }
    }
}
