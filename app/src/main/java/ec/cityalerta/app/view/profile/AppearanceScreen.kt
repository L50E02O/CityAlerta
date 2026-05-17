package ec.cityalerta.app.view.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ec.cityalerta.app.theme.LocalThemeManager
import ec.cityalerta.app.theme.ThemePreference
import ec.cityalerta.app.view.profile.components.ProfileSettingsScaffold
import ec.cityalerta.app.view.profile.components.ThemePreviewCard

@Composable
fun AppearanceScreen(navController: NavController) {
    val themeManager = LocalThemeManager.current
    val currentTheme by themeManager.themeState

    ProfileSettingsScaffold(navController = navController, title = "Apariencia") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                "Apariencia",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B2633)
            )
            Text(
                "Elige el tema de la aplicacion.",
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                fontSize = 14.sp,
                color = Color(0xFF6C757D)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThemePreviewCard(
                    label = "Light Mode",
                    isSelected = currentTheme == ThemePreference.LIGHT,
                    isDarkPreview = false,
                    onClick = { themeManager.setTheme(ThemePreference.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                ThemePreviewCard(
                    label = "Dark Mode",
                    isSelected = currentTheme == ThemePreference.DARK,
                    isDarkPreview = true,
                    onClick = { themeManager.setTheme(ThemePreference.DARK) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                            "System Default",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1B2633)
                        )
                        Text(
                            "Ajustar automaticamente al tema del sistema de tu dispositivo.",
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 13.sp,
                            color = Color(0xFF6C757D),
                            lineHeight = 18.sp
                        )
                    }
                    Switch(
                        checked = currentTheme == ThemePreference.SYSTEM,
                        onCheckedChange = { enabled ->
                            themeManager.setTheme(
                                if (enabled) ThemePreference.SYSTEM else ThemePreference.LIGHT
                            )
                        },
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
