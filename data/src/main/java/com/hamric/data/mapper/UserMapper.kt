package com.hamric.data.mapper

import com.hamric.core.database.entity.UserEntity
import com.hamric.core.network.dto.UsersResponseDto
import com.hamric.domain.model.User

fun UsersResponseDto.toEntity() = UserEntity(
    id = id, login = login, avatarUrl = avatarUrl, htmlUrl = htmlUrl
)

fun UserEntity.toDomain() = User(
    id = id, login = login, avatarUrl = avatarUrl, htmlUrl = htmlUrl
)