package com.hamric.domain.usecase

import com.hamric.domain.model.User
import com.hamric.domain.repository.UsersRepository
import kotlinx.coroutines.flow.Flow

class ObserveAndFilterCachedUsersUseCase(private val repository: UsersRepository) {
    operator fun invoke(query: String): Flow<List<User>> = repository.observeAndFilterCachedUsers(query)
}