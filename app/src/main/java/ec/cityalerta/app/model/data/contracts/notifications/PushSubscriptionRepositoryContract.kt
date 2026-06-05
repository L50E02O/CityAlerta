package ec.cityalerta.app.model.data.contracts.notifications

import ec.cityalerta.app.model.data.push.PushSubscription
import ec.cityalerta.app.model.data.push.PushSubscriptionCreateDto

interface PushSubscriptionRepositoryContract {
    suspend fun saveOrUpdate(entity: PushSubscriptionCreateDto): Result<PushSubscription>
    suspend fun deleteByToken(token: String): Result<Unit>
}
