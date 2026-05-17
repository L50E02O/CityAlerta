package ec.cityalerta.app.view.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val ScreenBackground = Color(0xFFF6F7F9)
private val TitleColor = Color(0xFF1B2633)
private val HintColor = Color(0xFF6C757D)
private val AccentBlue = Color(0xFF3B5B7A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScaffold(
    navController: NavController,
    title: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        color = AccentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atras",
                            tint = AccentBlue
                        )
                    }
                }
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = ScreenBackground
        ) {
            content()
        }
    }
}

@Composable
fun SettingsSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = HintColor,
        letterSpacing = 1.sp
    )
}

@Composable
fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EEF4)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = TitleColor, fontSize = 15.sp)
                Text(subtitle, color = HintColor, fontSize = 13.sp)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = HintColor
            )
        }
    }
}

@Composable
fun SettingsInfoField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F2F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = HintColor,
                    letterSpacing = 0.8.sp
                )
                Text(
                    value,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TitleColor
                )
            }
            trailing?.invoke()
        }
    }
}

@Composable
fun SettingsEditIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = AccentBlue, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun ThemePreviewCard(
    label: String,
    isSelected: Boolean,
    isDarkPreview: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThemePreviewFrame(isSelected = isSelected, isDarkPreview = isDarkPreview)
        Text(
            label,
            modifier = Modifier.padding(top = 10.dp),
            fontWeight = FontWeight.SemiBold,
            color = TitleColor,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ThemePreviewFrame(isSelected: Boolean, isDarkPreview: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AccentBlue else Color(0xFFDEE2E6),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkPreview) Color(0xFF0F1720) else Color.White)
            .padding(12.dp)
    ) {
        ThemePreviewSkeleton(isDarkPreview = isDarkPreview)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(AccentBlue),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .border(1.dp, Color(0xFFCED4DA), CircleShape)
            )
        }
    }
}

@Composable
private fun ThemePreviewSkeleton(isDarkPreview: Boolean) {
    val skeletonColor = if (isDarkPreview) Color(0xFF2A3A4D) else Color(0xFFE9ECEF)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(4.dp))
                .background(skeletonColor)
                .padding(vertical = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .clip(RoundedCornerShape(4.dp))
                .background(skeletonColor)
                .padding(vertical = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .clip(RoundedCornerShape(6.dp))
                .background(AccentBlue)
                .padding(vertical = 8.dp)
        )
    }
}
