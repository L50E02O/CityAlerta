package ec.cityalerta.app.view.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import ec.cityalerta.app.theme.DeepNavyBlack
import ec.cityalerta.app.theme.MapPlaceholderGreen

object ReportUiColors {
    @Composable
    fun screenBackground() = MaterialTheme.colorScheme.background
    
    @Composable
    fun cardBackground() = MaterialTheme.colorScheme.surface
    
    val FrameBackground = DeepNavyBlack
    val FrameBorder = Color.White
    
    @Composable
    fun hintText() = MaterialTheme.colorScheme.onSurfaceVariant
    
    @Composable
    fun accentRed() = MaterialTheme.colorScheme.error

    val MapPlaceholder = MapPlaceholderGreen
}

object ReportUiDimens {
    val ScreenPadding = 20.dp
    val SectionSpacing = 16.dp
    val FrameHeight = 360.dp
    val MapHeight = 190.dp
    val CaptureButtonSize = 72.dp
    val SideButtonSize = 56.dp
}

object ReportUiShapes {
    val Card = RoundedCornerShape(12.dp)
    val Frame = RoundedCornerShape(24.dp)
    val FrameInner = RoundedCornerShape(20.dp)
    val Button = RoundedCornerShape(18.dp)
    val SideButton = RoundedCornerShape(16.dp)
    val Circle = CircleShape
}