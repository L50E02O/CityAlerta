package ec.cityalerta.app.view.profile

import android.app.Activity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ec.cityalerta.app.R
import ec.cityalerta.app.model.utils.EmailValidator
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.theme.AppLanguage
import ec.cityalerta.app.theme.LocalLocaleManager
import ec.cityalerta.app.theme.SuccessGreen
import ec.cityalerta.app.view.components.ProfileAvatar
import ec.cityalerta.app.view.profile.components.LanguagePickerSheet
import ec.cityalerta.app.view.profile.components.SettingsEditIcon
import ec.cityalerta.app.view.profile.components.SettingsInfoField
import ec.cityalerta.app.view.profile.components.SettingsNavigationRow
import ec.cityalerta.app.viewmodel.ProfileViewModel
import ec.cityalerta.app.viewmodel.ProfileState

@Composable
internal fun SettingsMainContent(
    state: ProfileState,
    userEmail: String,
    languageSubtitle: String,
    notificationsEnabled: Boolean,
    onEditName: () -> Unit,
    onEditEmail: () -> Unit,
    onEditLocation: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenDelete: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            stringResource(R.string.settings_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            stringResource(R.string.settings_subtitle),
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        SettingsStatusMessages(state)

        SettingsProfileCard(state)

        Spacer(modifier = Modifier.height(20.dp))

        SettingsInfoField(
            label = stringResource(R.string.settings_full_name_label),
            value = state.fullName.ifBlank { stringResource(R.string.settings_not_available) },
            trailing = {
                SettingsEditIcon(onClick = onEditName)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsInfoField(
            label = stringResource(R.string.settings_email_label),
            value = userEmail.ifBlank { stringResource(R.string.settings_not_available) },
            trailing = {
                SettingsEditIcon(onClick = onEditEmail)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsInfoField(
            label = stringResource(R.string.settings_location_label),
            value = state.cityName.ifBlank { stringResource(R.string.profile_city_unavailable) },
            trailing = {
                SettingsEditIcon(onClick = onEditLocation)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsNotificationsCard(
            enabled = notificationsEnabled,
            onToggle = onToggleNotifications
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsNavigationRow(
            icon = Icons.Default.Language,
            title = stringResource(R.string.settings_language_title),
            subtitle = languageSubtitle,
            onClick = onOpenLanguage
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsDeleteCard(onOpenDelete)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsNotificationsCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
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
                    stringResource(R.string.settings_notifications_title),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.settings_notifications_desc),
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun SettingsStatusMessages(state: ProfileState) {
    state.settingsInfoMessage?.let { message ->
        Text(
            message,
            color = SuccessGreen, // Standard Material Success Green
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
    state.errorMessage?.let { message ->
        Text(
            message,
            color = MaterialTheme.colorScheme.error,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun SettingsProfileCard(state: ProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileAvatar(
                imageUrl = state.profileImageUrl,
                size = 72.dp,
                isLoading = state.isUploadingImage
            )
            Column {
                Text(
                    state.fullName.ifBlank { stringResource(R.string.profile_user_fallback) },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.profile_active_member),
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsDeleteCard(onOpenDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.settings_delete_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                stringResource(R.string.settings_delete_desc),
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Button(
                onClick = onOpenDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.settings_delete_button), color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
internal fun SettingsNameDialog(
    visible: Boolean,
    editName: String,
    isSaving: Boolean,
    onEditNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.settings_edit_name_title)) },
        text = {
            OutlinedTextField(
                value = editName,
                onValueChange = onEditNameChange,
                label = { Text(stringResource(R.string.settings_edit_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.settings_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.settings_cancel))
            }
        }
    )
}

@Composable
internal fun SettingsEmailDialog(
    visible: Boolean,
    editEmail: String,
    emailFieldError: String?,
    isSaving: Boolean,
    onEditEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (!visible) return

    val isEmailValid = EmailValidator.isValid(editEmail)
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.settings_edit_email_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = editEmail,
                    onValueChange = onEditEmailChange,
                    label = { Text(stringResource(R.string.settings_edit_email_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = editEmail.isNotEmpty() && !isEmailValid,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (editEmail.isNotEmpty() && !isEmailValid) {
                    Text(
                        stringResource(R.string.auth_email_invalid),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    stringResource(R.string.settings_edit_email_info),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                emailFieldError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !isSaving && isEmailValid) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.settings_save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.settings_cancel))
            }
        }
    )
}

@Composable
internal fun SettingsDeleteDialog(
    visible: Boolean,
    isDeleting: Boolean,
    deleteErrorMessage: String,
    navController: NavController,
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text(stringResource(R.string.settings_delete_dialog_title)) },
        text = {
            Text(
                if (isDeleting) {
                    stringResource(R.string.settings_deleting_account)
                } else {
                    stringResource(R.string.settings_delete_dialog_message)
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.deleteAccount(
                        successMessage = "",
                        errorMessage = deleteErrorMessage
                    ) {
                        onDismiss()
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                enabled = !isDeleting
            ) {
                Text(stringResource(R.string.settings_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(stringResource(R.string.settings_cancel))
            }
        }
    )
}

@Composable
internal fun SettingsLanguagePicker(
    visible: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val localeManager = LocalLocaleManager.current

    LanguagePickerSheet(
        title = stringResource(R.string.settings_language_dialog_title),
        currentLanguage = currentLanguage,
        spanishLabel = stringResource(R.string.settings_language_spanish),
        englishLabel = stringResource(R.string.settings_language_english),
        systemLabel = stringResource(R.string.settings_language_system),
        onDismiss = onDismiss,
        onLanguageSelected = { selected ->
            if (selected != currentLanguage) {
                localeManager.setLanguage(selected)
                onDismiss()
                (context as? Activity)?.recreate()
            } else {
                onDismiss()
            }
            onLanguageSelected(selected)
        }
    )
}

@Composable
internal fun SettingsNameDialogCloser(
    isSaving: Boolean,
    settingsInfoMessage: String?,
    pendingSave: Boolean,
    onClose: () -> Unit
) {
    LaunchedEffect(isSaving, settingsInfoMessage, pendingSave) {
        if (pendingSave && !isSaving && settingsInfoMessage != null) {
            onClose()
        }
    }
}

@Composable
internal fun SettingsEmailDialogCloser(
    isSaving: Boolean,
    settingsInfoMessage: String?,
    pendingSave: Boolean,
    onClose: () -> Unit
) {
    LaunchedEffect(isSaving, settingsInfoMessage, pendingSave) {
        if (pendingSave && !isSaving && settingsInfoMessage != null) {
            onClose()
        }
    }
}
