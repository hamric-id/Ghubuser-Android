package com.hamric.domain.repository

import com.hamric.core.common.Resource
import com.hamric.domain.SearchUsersInfo
import com.hamric.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UsersRepository {
    suspend fun searchUsers(query:String, page: Int=1, maxResultPerPage: Int=20): Resource<SearchUsersInfo>
    fun observeAndFilterCachedUsers(query: String): Flow<List<User>>
    suspend fun fecthUsers(since: Long): Resource<Long>
    fun observeCachedUsers():Flow<List<User>>
    suspend fun lastListSince(): Long
}