package com.hamric.domain.usecase

import com.hamric.core.common.Resource
import com.hamric.domain.repository.UsersRepository

class FetchUsersUseCase(private val repository: UsersRepository)  {
    suspend operator fun invoke(since: Long): Resource<Long> = repository.fecthUsers(since)
}