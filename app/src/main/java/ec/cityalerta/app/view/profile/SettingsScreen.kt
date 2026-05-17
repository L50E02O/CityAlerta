package ec.cityalerta.app.view.profile

import android.app.Activity
import ec.cityalerta.app.model.utils.EmailValidator
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ec.cityalerta.app.navigation.Routes
import ec.cityalerta.app.theme.AppLanguage
import ec.cityalerta.app.theme.LocalLocaleManager
import ec.cityalerta.app.view.components.ProfileAvatar
import ec.cityalerta.app.view.profile.components.ProfileSettingsScaffold
import ec.cityalerta.app.view.profile.components.SettingsEditIcon
import ec.cityalerta.app.view.profile.components.SettingsInfoField
import ec.cityalerta.app.view.profile.components.LanguagePickerSheet
import ec.cityalerta.app.view.profile.components.SettingsNavigationRow
import ec.cityalerta.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()
    val localeManager = LocalLocaleManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userEmail by remember { mutableStateOf("") }
    var showNameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var emailFieldError by remember { mutableStateOf<String?>(null) }
    var pendingNameSave by remember { mutableStateOf(false) }
    var pendingEmailSave by remember { mutableStateOf(false) }

    val nameEmptyMessage = stringResource(R.string.settings_name_empty)
    val nameUpdatedMessage = stringResource(R.string.settings_name_updated)
    val nameUpdateErrorMessage = stringResource(R.string.settings_name_update_error)
    val emailInvalidMessage = stringResource(R.string.auth_email_invalid)
    val emailSameMessage = stringResource(R.string.settings_email_same)
    val emailUpdatedMessage = stringResource(R.string.settings_email_updated)
    val emailUpdateErrorMessage = stringResource(R.string.settings_email_update_error)

    val currentLanguage = localeManager.languageState.value
    val languageSubtitle = when (currentLanguage) {
        AppLanguage.SPANISH -> stringResource(R.string.settings_language_spanish)
        AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
        AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
    }

    LaunchedEffect(Unit) {
        viewModel.loadSummaryIfNeeded()
        userEmail = viewModel.getUserEmail().orEmpty()
    }

    LaunchedEffect(state.settingsInfoMessage) {
        if (state.settingsInfoMessage?.contains("email", ignoreCase = true) == true ||
            state.settingsInfoMessage?.contains("correo", ignoreCase = true) == true
        ) {
            userEmail = viewModel.getUserEmail().orEmpty()
        }
    }

    ProfileSettingsScaffold(
        navController = navController,
        title = stringResource(R.string.settings_title)
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
                color = Color(0xFF1B2633)
            )
            Text(
                stringResource(R.string.settings_subtitle),
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                fontSize = 14.sp,
                color = Color(0xFF6C757D),
                lineHeight = 20.sp
            )

            state.settingsInfoMessage?.let { message ->
                Text(
                    message,
                    color = Color(0xFF1B5E20),
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                            color = Color(0xFF1B2633)
                        )
                        Text(
                            stringResource(R.string.profile_active_member),
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B5B7A),
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsInfoField(
                label = stringResource(R.string.settings_full_name_label),
                value = state.fullName.ifBlank { stringResource(R.string.settings_not_available) },
                trailing = {
                    SettingsEditIcon {
                        editName = state.fullName
                        emailFieldError = null
                        viewModel.clearSettingsMessages()
                        showNameDialog = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsInfoField(
                label = stringResource(R.string.settings_email_label),
                value = userEmail.ifBlank { stringResource(R.string.settings_not_available) },
                trailing = {
                    SettingsEditIcon {
                        editEmail = userEmail
                        emailFieldError = null
                        viewModel.clearSettingsMessages()
                        showEmailDialog = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsInfoField(
                label = stringResource(R.string.settings_location_label),
                value = state.cityName.ifBlank { stringResource(R.string.profile_city_unavailable) },
                trailing = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF3B5B7A),
                        modifier = Modifier.size(22.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsNavigationRow(
                icon = Icons.Default.Language,
                title = stringResource(R.string.settings_language_title),
                subtitle = languageSubtitle,
                onClick = {
                    viewModel.clearSettingsMessages()
                    showLanguageDialog = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8E8E8))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.settings_delete_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1B2633)
                    )
                    Text(
                        stringResource(R.string.settings_delete_desc),
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = 13.sp,
                        color = Color(0xFF6C757D),
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                    ) {
                        Text(stringResource(R.string.settings_delete_button), color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { if (!state.isSavingSettings) showNameDialog = false },
            title = { Text(stringResource(R.string.settings_edit_name_title)) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text(stringResource(R.string.settings_edit_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSavingSettings
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingNameSave = true
                        viewModel.updateFullName(
                            newName = editName,
                            emptyNameMessage = nameEmptyMessage,
                            successMessage = nameUpdatedMessage,
                            errorMessage = nameUpdateErrorMessage
                        )
                    },
                    enabled = !state.isSavingSettings
                ) {
                    if (state.isSavingSettings) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.settings_save))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }, enabled = !state.isSavingSettings) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }

    LaunchedEffect(state.isSavingSettings, state.settingsInfoMessage, pendingNameSave) {
        if (pendingNameSave && !state.isSavingSettings) {
            if (state.settingsInfoMessage != null) {
                showNameDialog = false
            }
            pendingNameSave = false
        }
    }

    if (showEmailDialog) {
        val isEmailValid = EmailValidator.isValid(editEmail)
        AlertDialog(
            onDismissRequest = { if (!state.isSavingSettings) showEmailDialog = false },
            title = { Text(stringResource(R.string.settings_edit_email_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = {
                            editEmail = it
                            emailFieldError = null
                        },
                        label = { Text(stringResource(R.string.settings_edit_email_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = editEmail.isNotEmpty() && !isEmailValid,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSavingSettings
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
                        color = Color(0xFF6C757D)
                    )
                    emailFieldError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingEmailSave = true
                        when (
                            viewModel.updateEmail(
                                newEmail = editEmail,
                                currentEmail = userEmail,
                                invalidEmailMessage = emailInvalidMessage,
                                sameEmailMessage = emailSameMessage,
                                successMessage = emailUpdatedMessage,
                                errorMessage = emailUpdateErrorMessage
                            )
                        ) {
                            ProfileViewModel.EmailUpdateValidation.INVALID -> {
                                pendingEmailSave = false
                                emailFieldError = emailInvalidMessage
                            }
                            ProfileViewModel.EmailUpdateValidation.SAME -> {
                                pendingEmailSave = false
                                emailFieldError = emailSameMessage
                            }
                            ProfileViewModel.EmailUpdateValidation.OK -> Unit
                        }
                    },
                    enabled = !state.isSavingSettings && isEmailValid
                ) {
                    if (state.isSavingSettings) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.settings_save))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailDialog = false }, enabled = !state.isSavingSettings) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }

    LaunchedEffect(state.isSavingSettings, state.settingsInfoMessage, pendingEmailSave) {
        if (pendingEmailSave && !state.isSavingSettings) {
            if (state.settingsInfoMessage != null) {
                userEmail = viewModel.getUserEmail().orEmpty().ifBlank { editEmail.trim() }
                showEmailDialog = false
            }
            pendingEmailSave = false
        }
    }

    if (showLanguageDialog) {
        LanguagePickerSheet(
            title = stringResource(R.string.settings_language_dialog_title),
            currentLanguage = currentLanguage,
            spanishLabel = stringResource(R.string.settings_language_spanish),
            englishLabel = stringResource(R.string.settings_language_english),
            systemLabel = stringResource(R.string.settings_language_system),
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { selected ->
                if (selected != currentLanguage) {
                    localeManager.setLanguage(selected)
                    showLanguageDialog = false
                    (context as? Activity)?.recreate()
                } else {
                    showLanguageDialog = false
                }
            }
        )
    }

    if (showDeleteDialog) {
        val deleteErrorMessage = stringResource(R.string.settings_delete_error)
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeletingAccount) {
                    showDeleteDialog = false
                }
            },
            title = { Text(stringResource(R.string.settings_delete_dialog_title)) },
            text = {
                Text(
                    if (state.isDeletingAccount) {
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
                            showDeleteDialog = false
                            navController.navigate(Routes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    enabled = !state.isDeletingAccount
                ) {
                    Text(stringResource(R.string.settings_confirm), color = Color(0xFFE74C3C))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !state.isDeletingAccount
                ) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }
}
