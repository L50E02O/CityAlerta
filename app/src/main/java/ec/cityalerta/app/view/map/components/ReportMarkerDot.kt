package ec.cityalerta.app.view.map.components

import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import ec.cityalerta.app.model.data.MapMarker
import ec.cityalerta.app.model.data.reporte.ReportType
import ec.cityalerta.app.theme.*
import androidx.core.graphics.createBitmap

@Composable
fun ReportMarkerDot(
    marker: MapMarker,
    reportType: ReportType,
    onClick: () -> Unit
) {
    val markerState = rememberMarkerState(position = LatLng(marker.latitude, marker.longitude))
    
    val dotIcon = remember(reportType) {
        val color = getCategoryColor(reportType)
        createPureDotIcon(color)
    }

    Marker(
        state = markerState,
        icon = dotIcon,
        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
        onClick = {
            onClick()
            true
        }
    )
}

private fun getCategoryColor(type: ReportType): Color {
    return when (type) {
        ReportType.ZONA_DE_RIESGO -> CategoryRisk
        ReportType.BACHE -> CategoryPothole
        ReportType.AGUA -> CategoryWater
        ReportType.LUZ -> CategoryLight
    }
}

private fun createPureDotIcon(color: Color): BitmapDescriptor {
    val size = 48
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val center = size / 2f
    val radius = size / 3.5f
    
    // Sombra más estética (más difusa y centrada ligeramente hacia abajo)
    val shadowPaint = Paint().apply {
        this.color = android.graphics.Color.BLACK
        alpha = 70
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(center, center + 2f, radius + 1f, shadowPaint)

    val strokePaint = Paint().apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(center, center, radius + 2f, strokePaint)

    // El punto de color principal
    val paint = Paint().apply {
        this.color = color.toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(center, center, radius, paint)

    val glossPaint = Paint().apply {
        this.color = android.graphics.Color.WHITE
        alpha = 80
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(center - radius/3f, center - radius/3f, radius/4f, glossPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
