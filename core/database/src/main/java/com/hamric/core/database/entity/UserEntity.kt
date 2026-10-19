package com.hamric.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="users")
data class UserEntity (
    @PrimaryKey val id: Long,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val cachedAt: Long = System.currentTimeMillis()
)