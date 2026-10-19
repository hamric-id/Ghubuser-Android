package com.hamric.domain.usecase

import com.hamric.core.common.Resource
import com.hamric.domain.model.DetailUser
import com.hamric.domain.repository.DetailUserRepository

class FetchDetailUserUseCase(private val repository: DetailUserRepository) {
    suspend operator fun invoke(login: String): Resource<DetailUser> {
        if (login.isBlank()) {
            return Resource.Error(IllegalArgumentException("login is blank"))
        }
        return repository.fetchDetailUser(login.trim())
    }
}