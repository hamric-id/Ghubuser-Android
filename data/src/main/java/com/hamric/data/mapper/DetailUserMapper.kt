package com.hamric.data.mapper

import com.hamric.core.database.entity.DetailUserEntity
import com.hamric.core.network.dto.DetailUserResponseDto
import com.hamric.domain.model.DetailUser

fun DetailUserResponseDto.toDomain(): DetailUser = DetailUser(
    login = login,
    id = id,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    name = name,
    company = company,
    blog = blog,
    location = location,
    bio = bio,
    twitterUsername = twitterUsername,
    publicRepos = publicRepos,
    publicGists = publicGists,
    followers = followers,
    following = following,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun DetailUserResponseDto.toEntity(now: Long): DetailUserEntity = DetailUserEntity(
    login = login,
    id = id,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    name = name,
    company = company,
    blog = blog,
    location = location,
    bio = bio,
    twitterUsername = twitterUsername,
    publicRepos = publicRepos,
    publicGists = publicGists,
    followers = followers,
    following = following,
    createdAt = createdAt,
    updatedAt = updatedAt,
    cachedAt = now,
    lastAccessedAt = now
)

fun DetailUserEntity.toDomain(): DetailUser = DetailUser(
    login = login,
    id = id,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    name = name,
    company = company,
    blog = blog,
    location = location,
    bio = bio,
    twitterUsername = twitterUsername,
    publicRepos = publicRepos,
    publicGists = publicGists,
    followers = followers,
    following = following,
    createdAt = createdAt,
    updatedAt = updatedAt
)