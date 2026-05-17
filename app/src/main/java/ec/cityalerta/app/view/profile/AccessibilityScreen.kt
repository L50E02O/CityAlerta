package ec.cityalerta.app.view.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ec.cityalerta.app.theme.AccessibilityManager
import ec.cityalerta.app.theme.LocalAccessibilityManager
import ec.cityalerta.app.view.profile.components.ProfileSettingsScaffold
import ec.cityalerta.app.view.profile.components.SettingsSectionLabel

@Composable
fun AccessibilityScreen(navController: NavController) {
    val accessibilityManager = LocalAccessibilityManager.current
    val textScale by accessibilityManager.textScaleState
    val highContrast by accessibilityManager.highContrastState

    ProfileSettingsScaffold(navController = navController, title = "Accesibilidad") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SettingsSectionLabel("Visualizacion")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = null,
                            tint = Color(0xFF3B5B7A),
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .padding(6.dp)
                        )
                        Text(
                            "Tamano de texto",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1B2633)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A", fontSize = 12.sp, color = Color(0xFF6C757D))
                        Slider(
                            value = textScale,
                            onValueChange = { accessibilityManager.setTextScale(it) },
                            valueRange = AccessibilityManager.MIN_TEXT_SCALE..AccessibilityManager.MAX_TEXT_SCALE,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B5B7A),
                                activeTrackColor = Color(0xFF3B5B7A),
                                inactiveTrackColor = Color(0xFFD6E4F0)
                            )
                        )
                        Text("A", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B2633))
                    }

                    Text(
                        "Arrastra para ajustar el tamano de lectura",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF6C757D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSectionLabel("Asistencia")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Alto contraste",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1B2633)
                        )
                        Text(
                            "Mejora la legibilidad con colores mas definidos.",
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 13.sp,
                            color = Color(0xFF6C757D)
                        )
                    }
                    Switch(
                        checked = highContrast,
                        onCheckedChange = { accessibilityManager.setHighContrast(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF3B5B7A),
                            checkedThumbColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
