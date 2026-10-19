package com.hamric.data.repository

import com.hamric.core.common.DispatchersProvider
import com.hamric.core.common.Resource
import com.hamric.core.database.dao.PaginationDao
import com.hamric.core.database.entity.PaginationEntity
import com.hamric.core.database.dao.UsersDao
import com.hamric.core.network.GithubApi
import com.hamric.domain.SearchUsersInfo
import com.hamric.data.mapper.toDomain
import com.hamric.data.mapper.toEntity
import com.hamric.domain.model.User
import com.hamric.domain.repository.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UsersRepositoryImpl(
    private val api: GithubApi,
    private val usersDao: UsersDao,
    private val paginationDao: PaginationDao,
    private val dispatchers: DispatchersProvider,
): UsersRepository {
    override suspend fun searchUsers(
        query: String,
        page: Int,
        maxResultPerPage :Int
    ): Resource<SearchUsersInfo> =
        withContext(dispatchers.io){
            try{
                val usersDto = api.searchUsers(query,page)

                usersDao.insertAll(usersDto.items.map{it.toEntity()})

                val info = SearchUsersInfo(
                    totalCount = usersDto.totalCount,
                    perPage = maxResultPerPage,
                    currentPage = page
                )

                Resource.Success(info)
            }catch(t: Throwable){
                Resource.Error(t)
            }
        }

    override fun observeAndFilterCachedUsers(query: String): Flow<List<User>> =
        usersDao.observeAndFilter(query).map{list -> list.map{it.toDomain() }}


    override suspend fun fecthUsers(since: Long): Resource<Long> =
        withContext(dispatchers.io){
            try{
                val usersDto = api.listUsers(since)
                if (usersDto.isEmpty()){
                    Resource.Success(since)
                }

                usersDao.insertAll(usersDto.map{it.toEntity()})

                val newSince = usersDto.maxOf{it.id}
                paginationDao.upsert(
                    PaginationEntity(PaginationEntity.KEY_LIST_USERS, newSince)
                )
                Resource.Success(newSince)
            }catch(t: Throwable){
                Resource.Error(t)
            }
        }

    override fun observeCachedUsers(): Flow<List<User>> =
        usersDao.observe().map{list -> list.map{it.toDomain() }}

    override suspend fun lastListSince(): Long =
        paginationDao.get(PaginationEntity.KEY_LIST_USERS)?.lastSinceId?:0L
}