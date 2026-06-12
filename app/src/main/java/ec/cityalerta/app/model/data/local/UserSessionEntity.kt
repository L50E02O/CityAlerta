package ec.cityalerta.app.model.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val roomId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
)
