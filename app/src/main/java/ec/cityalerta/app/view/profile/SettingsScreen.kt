package ec.cityalerta.app.view.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import ec.cityalerta.app.R
import ec.cityalerta.app.theme.AppLanguage
import ec.cityalerta.app.theme.LocalLocaleManager
import ec.cityalerta.app.view.components.CityPickerSheet
import ec.cityalerta.app.view.profile.components.ProfileSettingsScaffold
import ec.cityalerta.app.viewmodel.AuthViewModel
import ec.cityalerta.app.viewmodel.ProfileViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel
) {
    val state by viewModel.state.collectAsState()
    val authState = authViewModel.uiState
    val localeManager = LocalLocaleManager.current
    val context = LocalContext.current
    val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val activity = context as? Activity

    var hasRequestedNotificationPermission by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            authViewModel.onNotificationsPermissionGranted()
        } else {
            authViewModel.onNotificationsPermissionDenied()
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } ?: false
            if (!shouldShowRationale && hasRequestedNotificationPermission) {
                showNotificationSettingsDialog = true
            }
        }
    }

    var showNameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var emailFieldError by remember { mutableStateOf<String?>(null) }
    var pendingNameSave by remember { mutableStateOf(false) }
    var pendingEmailSave by remember { mutableStateOf(false) }
    var pendingLocationSave by remember { mutableStateOf(false) }

    val nameEmptyMessage = stringResource(R.string.settings_name_empty)
    val nameUpdatedMessage = stringResource(R.string.settings_name_updated)
    val nameUpdateErrorMessage = stringResource(R.string.settings_name_update_error)
    val emailInvalidMessage = stringResource(R.string.auth_email_invalid)
    val emailSameMessage = stringResource(R.string.settings_email_same)
    val emailUpdatedMessage = stringResource(R.string.settings_email_updated)
    val emailUpdateErrorMessage = stringResource(R.string.settings_email_update_error)
    val cityUpdatedMessage = stringResource(R.string.settings_language_changed) // Podriamos crear uno mas especifico pero reusamos este por ahora
    val cityUpdateErrorMessage = stringResource(R.string.auth_city_load_error)
    val deleteErrorMessage = stringResource(R.string.settings_delete_error)

    val currentLanguage = localeManager.languageState.value
    val languageSubtitle = when (currentLanguage) {
        AppLanguage.SPANISH -> stringResource(R.string.settings_language_spanish)
        AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
        AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
    }

    LaunchedEffect(Unit) {
        viewModel.loadSummaryIfNeeded()
    }

    ProfileSettingsScaffold(
        navController = navController,
        title = stringResource(R.string.settings_title)
    ) {
        SettingsMainContent(
            state = state,
            userEmail = state.userEmail,
            languageSubtitle = languageSubtitle,
            notificationsEnabled = authState.notificationsEnabled,
            onEditName = {
                editName = state.fullName
                emailFieldError = null
                viewModel.clearSettingsMessages()
                showNameDialog = true
            },
            onEditEmail = {
                editEmail = state.userEmail
                emailFieldError = null
                viewModel.clearSettingsMessages()
                showEmailDialog = true
            },
            onEditLocation = {
                viewModel.clearSettingsMessages()
                viewModel.loadCities()
                showLocationDialog = true
            },
            onOpenLanguage = {
                viewModel.clearSettingsMessages()
                showLanguageDialog = true
            },
            onOpenDelete = { showDeleteDialog = true },
            onToggleNotifications = { enabled ->
                if (enabled) {
                    if (needsPermission) {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            authViewModel.onNotificationsPermissionGranted()
                        } else {
                            val shouldShowRationale = activity?.let {
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    it,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            } ?: false
                            if (!hasRequestedNotificationPermission || shouldShowRationale) {
                                hasRequestedNotificationPermission = true
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                showNotificationSettingsDialog = true
                            }
                        }
                    } else {
                        authViewModel.onNotificationsPermissionGranted()
                    }
                } else {
                    authViewModel.onNotificationsDisabledByUser()
                }
            }
        )
    }

    if (showNotificationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationSettingsDialog = false },
            title = { Text(stringResource(R.string.settings_notifications_permission_title)) },
            text = { Text(stringResource(R.string.settings_notifications_permission_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationSettingsDialog = false
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.settings_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationSettingsDialog = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }

    SettingsNameDialog(
        visible = showNameDialog,
        editName = editName,
        isSaving = state.isSavingSettings,
        onEditNameChange = { editName = it },
        onDismiss = { showNameDialog = false },
        onSave = {
            pendingNameSave = true
            viewModel.updateFullName(
                newName = editName,
                emptyNameMessage = nameEmptyMessage,
                successMessage = nameUpdatedMessage,
                errorMessage = nameUpdateErrorMessage
            )
        }
    )

    SettingsNameDialogCloser(
        isSaving = state.isSavingSettings,
        settingsInfoMessage = state.settingsInfoMessage,
        pendingSave = pendingNameSave,
        onClose = {
            showNameDialog = false
            pendingNameSave = false
        }
    )

    SettingsEmailDialog(
        visible = showEmailDialog,
        editEmail = editEmail,
        emailFieldError = emailFieldError,
        isSaving = state.isSavingSettings,
        onEditEmailChange = {
            editEmail = it
            emailFieldError = null
        },
        onDismiss = { showEmailDialog = false },
        onSave = {
            pendingEmailSave = true
            when (
                viewModel.updateEmail(
                    newEmail = editEmail,
                    currentEmail = state.userEmail,
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
        }
    )

    SettingsEmailDialogCloser(
        isSaving = state.isSavingSettings,
        settingsInfoMessage = state.settingsInfoMessage,
        pendingSave = pendingEmailSave,
        onClose = {
            showEmailDialog = false
            pendingEmailSave = false
        }
    )

    CityPickerSheet(
        visible = showLocationDialog,
        cities = state.cities,
        query = state.ciudadQuery,
        onQueryChange = { viewModel.onCiudadChange(it) },
        onDismiss = { showLocationDialog = false },
        onCitySelected = { ciudad ->
            pendingLocationSave = true
            viewModel.updateCity(
                ciudadId = ciudad.id,
                successMessage = cityUpdatedMessage,
                errorMessage = cityUpdateErrorMessage
            )
        }
    )

    SettingsNameDialogCloser(
        isSaving = state.isSavingSettings,
        settingsInfoMessage = state.settingsInfoMessage,
        pendingSave = pendingLocationSave,
        onClose = {
            showLocationDialog = false
            pendingLocationSave = false
        }
    )

    SettingsLanguagePicker(
        visible = showLanguageDialog,
        currentLanguage = currentLanguage,
        onDismiss = { showLanguageDialog = false },
        onLanguageSelected = { }
    )

    SettingsDeleteDialog(
        visible = showDeleteDialog,
        isDeleting = state.isDeletingAccount,
        deleteErrorMessage = deleteErrorMessage,
        navController = navController,
        viewModel = viewModel,
        onDismiss = { showDeleteDialog = false }
    )
}
