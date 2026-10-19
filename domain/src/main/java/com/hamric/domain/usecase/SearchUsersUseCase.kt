package com.hamric.domain.usecase

import com.hamric.core.common.Resource
import com.hamric.domain.SearchUsersInfo
import com.hamric.domain.repository.UsersRepository

class SearchUsersUseCase(private val repository: UsersRepository) {
    suspend operator fun invoke(query: String, page : Int = 1, maxResultPerPage :Int = 20): Resource<SearchUsersInfo> {
        if(query.isBlank()) return Resource.Success(SearchUsersInfo(0,maxResultPerPage,1))
        return repository.searchUsers(query, page)
    }
}