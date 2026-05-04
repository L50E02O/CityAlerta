package ec.cityalerta.app.model.repository.interfaces

interface ICrudRepository<T, CreateDto, UpdateDto> {
    suspend fun create(entity: CreateDto): Result<T>
    suspend fun update(entity: UpdateDto, id: String): Result<T>
    suspend fun getAll(): Result<List<T>>
    suspend fun getById(id: String): Result<T?>
    suspend fun delete(id: String): Result<Unit>
}

