package ec.cityalerta.app.di

import android.content.Context
import androidx.room.Room
import ec.cityalerta.app.model.data.contracts.auth.AuthRepositoryContract
import ec.cityalerta.app.model.data.contracts.session.SessionRepositoryContract
import ec.cityalerta.app.model.data.local.AppDatabase
import ec.cityalerta.app.model.repository.AuthRepository
import ec.cityalerta.app.model.repository.SessionRepository

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "cityalerta.db"
        ).build()
    }

    val sessionRepository: SessionRepositoryContract by lazy {
        SessionRepository(database.userSessionDao())
    }

    val authRepository: AuthRepositoryContract by lazy {
        AuthRepository(sessionRepository)
    }
}
