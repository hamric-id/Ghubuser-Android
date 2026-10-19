package com.hamric.data.repository

import com.hamric.core.common.DispatchersProvider
import com.hamric.core.common.Resource
import com.hamric.core.database.dao.DetailUserDao
import com.hamric.core.network.GithubApi
import com.hamric.data.mapper.toDomain
import com.hamric.data.mapper.toEntity
import com.hamric.domain.model.DetailUser
import com.hamric.domain.repository.DetailUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DetailUserRepositoryImpl(
    private val api: GithubApi,
    private val detailUserDao: DetailUserDao,
    private val dispatchers: DispatchersProvider,
    private val maxCacheSize: Int = MAX_CACHED_DETAILS
): DetailUserRepository {
    override fun observeCachedDetailUser(login: String): Flow<DetailUser?> =
        detailUserDao.observe(login).map { it?.toDomain() }

    override suspend fun fetchDetailUser(login: String): Resource<DetailUser> =
        withContext(dispatchers.io){
            try{
                val detailUserDto = api.detailUser(login)
                val now = System.currentTimeMillis()
                detailUserDao.upsert(detailUserDto.toEntity(now))
                detailUserDao.trimTo(maxCacheSize)
                Resource.Success(detailUserDto.toDomain())
            }catch(t: Throwable){
                Resource.Error(t)
            }
        }

    override suspend fun touch(login: String)=
        withContext(dispatchers.io) {
            detailUserDao.touch(login, System.currentTimeMillis())
        }
    companion object {
        const val MAX_CACHED_DETAILS = 200
    }
}