package ec.cityalerta.app.view.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import ec.cityalerta.app.R
import ec.cityalerta.app.theme.AppLanguage
import ec.cityalerta.app.theme.LocalLocaleManager
import ec.cityalerta.app.view.profile.components.ProfileSettingsScaffold
import ec.cityalerta.app.viewmodel.ProfileViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()
    val localeManager = LocalLocaleManager.current

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
            onOpenDelete = { showDeleteDialog = true }
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

    SettingsLocationDialog(
        visible = showLocationDialog,
        cities = state.cities,
        query = state.ciudadQuery,
        isSaving = state.isSavingSettings,
        onQueryChange = { viewModel.onCiudadChange(it) },
        onDismiss = { showLocationDialog = false },
        onSave = { selectedId ->
            pendingLocationSave = true
            viewModel.updateCity(
                ciudadId = selectedId,
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
