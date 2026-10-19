package com.hamric.core.network

import com.hamric.core.network.dto.DetailUserResponseDto
import com.hamric.core.network.dto.SearchUsersResponseDto
import com.hamric.core.network.dto.UsersResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {
    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): SearchUsersResponseDto

    @GET("users")
    suspend fun listUsers(
        @Query("since") since: Long = 0L,
        @Query("per_page") perPage: Int = 30
    ): List<UsersResponseDto>

    @GET("users/{username}")
    suspend fun detailUser(
        @Path("username") username: String
    ): DetailUserResponseDto
}