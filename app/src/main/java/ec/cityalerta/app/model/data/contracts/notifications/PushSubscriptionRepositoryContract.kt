package ec.cityalerta.app.model.data.contracts.notifications

import ec.cityalerta.app.model.data.push.PushSubscription
import ec.cityalerta.app.model.data.push.PushSubscriptionCreateDto
import ec.cityalerta.app.model.data.push.PushSubscriptionUpdateDto

interface PushSubscriptionRepositoryContract {
    suspend fun saveOrUpdate(entity: PushSubscriptionCreateDto): Result<PushSubscription>
    suspend fun getByToken(token: String): Result<PushSubscription?>
    suspend fun disableByToken(token: String): Result<Unit>
    suspend fun deleteByToken(token: String): Result<Unit>
    suspend fun updateByToken(token: String, entity: PushSubscriptionUpdateDto): Result<PushSubscription>
}
