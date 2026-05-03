package ec.cityalerta.app.model.repository.interfaces

interface ICrudRepository<T> {
    suspend fun create(entity: T): Result<T>
    suspend fun update(entity: T): Result<T>
    suspend fun getAll(): Result<List<T>>
    suspend fun getById(id: String): Result<T?>
    suspend fun delete(id: String): Result<Unit>
}

