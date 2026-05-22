package ec.cityalerta.app.view.map.components

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import ec.cityalerta.app.model.data.reporte.ReportType
import androidx.core.graphics.createBitmap

@Composable
fun ReportClusterMarker(
    position: LatLng,
    count: Int,
    reportType: ReportType,
    onClick: () -> Unit
) {
    val markerState = rememberMarkerState(position = position)
    val clusterIcon = remember(count, reportType) {
        val color = getCategoryColor(reportType)
        createClusterIcon(count, color)
    }

    Marker(
        state = markerState,
        icon = clusterIcon,
        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
        onClick = {
            onClick()
            true
        }
    )
}

private fun getCategoryColor(type: ReportType): Color {
    return when (type) {
        ReportType.ZONA_DE_RIESGO -> Color(0xFFFF5252)
        ReportType.BACHE -> Color(0xFFFFC107)
        ReportType.AGUA -> Color(0xFF2196F3)
        ReportType.LUZ -> Color(0xFF4CAF50)
    }
}

private fun createClusterIcon(count: Int, color: Color): BitmapDescriptor {
    val size = 80
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val center = size / 2f
    
    // Texto a mostrar
    val text = if (count > 20) "20+" else count.toString()

    val shadowPaint = Paint().apply {
        this.color = android.graphics.Color.BLACK
        alpha = 60
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(center, center + 4f, size * 0.42f, shadowPaint)

    val haloPaint = Paint().apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(center, center, size * 0.4f, haloPaint)

    // 3. Círculo de color principal
    val mainPaint = Paint().apply {
        this.color = color.toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(center, center, size * 0.35f, mainPaint)

    val glossPaint = Paint().apply {
        this.color = android.graphics.Color.WHITE
        alpha = 40
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(center - size*0.1f, center - size*0.1f, size*0.15f, glossPaint)

    val textPaint = Paint().apply {
        this.color = android.graphics.Color.WHITE
        textSize = 28f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    
    // Centrado vertical del texto
    val textBounds = Rect()
    textPaint.getTextBounds(text, 0, text.length, textBounds)
    val textY = center + (textBounds.height() / 2f)
    
    canvas.drawText(text, center, textY, textPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
