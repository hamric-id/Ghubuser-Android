package com.hamric.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hamric.core.database.entity.PaginationEntity

@Dao
interface PaginationDao {
    @Query("SELECT * FROM pagination_state WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): PaginationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PaginationEntity)
}