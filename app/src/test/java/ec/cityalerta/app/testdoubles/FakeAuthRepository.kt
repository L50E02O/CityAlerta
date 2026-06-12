package ec.cityalerta.app.testdoubles

import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.auth.AuthState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    var getCiudadIdResult: Result<String> = Result.success("ciudad-1"),
    var getRoomIdResult: Result<String> = Result.success("room-1"),
    var checkEmailVerifiedResult: Result<Boolean> = Result.success(false),
    var restoreSessionResult: Result<Unit> = Result.success(Unit),
    var refreshAndPersistSessionResult: Result<Unit> = Result.success(Unit),
    var shouldSucceed: Boolean = true
) : AuthRepositoryContract {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    private var _currentSession: Any? = null

    var verifyRecoveryEmailCalls: Int = 0
    var lastVerifyEmail: String? = null
    var lastResetEmail: String? = null
    var lastResetPassword: String? = null
    var lastCiudadNombre: String? = null
    var lastUpdateEmail: String? = null
    var deleteAccountCalls: Int = 0
    var checkEmailVerifiedCalls: Int = 0
    var signInCalls: Int = 0
    var signUpCalls: Int = 0

    override fun observeAuthState(): Flow<AuthState> = _authState.asStateFlow()

    override suspend fun signUp(email: String, password: String, ciudadId: String): Result<Unit> {
        delay(50)
        signUpCalls++
        return if (shouldSucceed) {
            _authState.value = AuthState.EmailVerificationPending(email)
            signUpResult
        } else {
            Result.failure(Exception("sign up failed"))
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        delay(50)
        signInCalls++
        return if (shouldSucceed) {
            _authState.value = AuthState.Authenticated("user-1")
            signInResult
        } else {
            Result.failure(Exception("sign in failed"))
        }
    }

    override suspend fun logOut(): Result<Unit> {
        delay(50)
        _authState.value = AuthState.Unauthenticated
        return logOutResult
    }

    override suspend fun checkEmailVerified(): Result<Boolean> {
        delay(50)
        checkEmailVerifiedCalls++
        if (checkEmailVerifiedResult.isSuccess && checkEmailVerifiedResult.getOrNull() == true) {
            _authState.value = AuthState.Authenticated("user-1")
        }
        return checkEmailVerifiedResult
    }

    override suspend fun restoreSession(accessToken: String, refreshToken: String): Result<Unit> {
        delay(50)
        return restoreSessionResult
    }

    override suspend fun refreshAndPersistSession(): Result<Unit> {
        delay(50)
        return refreshAndPersistSessionResult
    }

    override suspend fun verifyRecoveryEmail(email: String): Result<Boolean> {
        delay(50)
        verifyRecoveryEmailCalls++
        lastVerifyEmail = email
        return verifyRecoveryEmailResult
    }

    override suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<Unit> {
        delay(50)
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

    override suspend fun getRoomId() = getRoomIdResult

    override suspend fun buscarCiudadPorNombre(nombre: String): Result<String?> {
        lastCiudadNombre = nombre
        return buscarCiudadPorNombreResult
    }

    override fun getCurrentSession(): Any? = _currentSession

    fun setAuthState(state: AuthState) {
        _authState.value = state
    }
}
