package com.hamric.domain.repository

import com.hamric.core.common.Resource
import com.hamric.domain.model.DetailUser
import kotlinx.coroutines.flow.Flow

interface DetailUserRepository {
    fun observeCachedDetailUser(login: String) : Flow<DetailUser?>
    suspend fun fetchDetailUser(login: String): Resource<DetailUser>
    suspend fun touch(login: String)
}