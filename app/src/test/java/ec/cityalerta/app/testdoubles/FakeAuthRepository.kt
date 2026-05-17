package ec.cityalerta.app.testdoubles

import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract

/**
 * Doble de prueba para AuthRepositoryContract con resultados configurables.
 */
class FakeAuthRepository(
    var verifyRecoveryEmailResult: Result<Boolean> = Result.success(true),
    var resetPasswordByEmailResult: Result<Unit> = Result.success(Unit),
    var buscarCiudadPorNombreResult: Result<String?> = Result.success(null),
    var signUpResult: Result<Unit> = Result.success(Unit),
    var signInResult: Result<Unit> = Result.success(Unit),
    var logOutResult: Result<Unit> = Result.success(Unit),
    var resendSignupConfirmationResult: Result<Unit> = Result.success(Unit),
    var updatePasswordResult: Result<Unit> = Result.success(Unit),
    var updateEmailResult: Result<Unit> = Result.success(Unit),
    var deleteAccountResult: Result<Unit> = Result.success(Unit),
    var getUserIdResult: Result<String> = Result.success("user-1"),
    var getUserEmailResult: Result<String> = Result.success("test@example.com"),
    var getCiudadIdResult: Result<String> = Result.success("ciudad-1")
) : AuthRepositoryContract {

    var verifyRecoveryEmailCalls: Int = 0
    var lastVerifyEmail: String? = null
    var lastResetEmail: String? = null
    var lastResetPassword: String? = null
    var lastCiudadNombre: String? = null
    var lastUpdateEmail: String? = null
    var deleteAccountCalls: Int = 0

    override suspend fun signUp(email: String, password: String, ciudadId: String) = signUpResult

    override suspend fun signIn(email: String, password: String) = signInResult

    override suspend fun logOut() = logOutResult

    override suspend fun verifyRecoveryEmail(email: String): Result<Boolean> {
        verifyRecoveryEmailCalls++
        lastVerifyEmail = email
        return verifyRecoveryEmailResult
    }

    override suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<Unit> {
        lastResetEmail = email
        lastResetPassword = newPassword
        return resetPasswordByEmailResult
    }

    override suspend fun resendSignupConfirmation(email: String) = resendSignupConfirmationResult

    override suspend fun updatePassword(newPassword: String) = updatePasswordResult

    override suspend fun updateEmail(newEmail: String): Result<Unit> {
        lastUpdateEmail = newEmail
        return updateEmailResult
    }

    override suspend fun deleteAccount(): Result<Unit> {
        deleteAccountCalls++
        return deleteAccountResult
    }

    override suspend fun getUserId() = getUserIdResult

    override suspend fun getUserEmail() = getUserEmailResult

    override suspend fun getCiudadId() = getCiudadIdResult

    override suspend fun buscarCiudadPorNombre(nombre: String): Result<String?> {
        lastCiudadNombre = nombre
        return buscarCiudadPorNombreResult
    }
}
