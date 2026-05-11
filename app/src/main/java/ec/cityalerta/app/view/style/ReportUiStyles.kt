package ec.cityalerta.app.view.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape

object ReportUiColors {
    val ScreenBackground = Color(0xFFF6F2F8)
    val CardBackground = Color.White
    val FrameBackground = Color(0xFF0F1216)
    val FrameBorder = Color.White
    val HintText = Color(0xFF8A8D99)
    val AccentRed = Color(0xFFE64B4B)
    val MapPlaceholder = Color(0xFF365F52)
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