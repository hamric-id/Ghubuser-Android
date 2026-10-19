package com.hamric.domain.usecase

import com.hamric.domain.model.User
import com.hamric.domain.repository.UsersRepository
import kotlinx.coroutines.flow.Flow

class ObserveCachedUsersUseCase(private val repository: UsersRepository) {
    operator fun invoke(): Flow<List<User>> = repository.observeCachedUsers()
}
