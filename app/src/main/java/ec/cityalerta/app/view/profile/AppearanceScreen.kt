package ec.cityalerta.app.view.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ec.cityalerta.app.R
import ec.cityalerta.app.theme.LocalThemeManager
import ec.cityalerta.app.theme.ThemePreference
import ec.cityalerta.app.view.profile.components.ProfileSettingsScaffold
import ec.cityalerta.app.view.profile.components.ThemePreviewCard

@Composable
fun AppearanceScreen(navController: NavController) {
    val themeManager = LocalThemeManager.current
    val currentTheme by themeManager.themeState

    ProfileSettingsScaffold(navController = navController, title = stringResource(R.string.appearance_title)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 1100.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
            Text(
                stringResource(R.string.appearance_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                stringResource(R.string.appearance_subtitle),
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThemePreviewCard(
                    label = stringResource(R.string.appearance_light),
                    isSelected = currentTheme == ThemePreference.LIGHT,
                    isDarkPreview = false,
                    onClick = { themeManager.setTheme(ThemePreference.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                ThemePreviewCard(
                    label = stringResource(R.string.appearance_dark),
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.appearance_system),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(R.string.appearance_system_desc),
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }
}
