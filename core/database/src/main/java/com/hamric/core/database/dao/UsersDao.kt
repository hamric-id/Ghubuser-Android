package com.hamric.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hamric.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface UsersDao {
    @Query("SELECT * FROM users WHERE login LIKE '%' || :query || '%' ORDER BY login ASC")
    fun observeAndFilter(query: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun observe(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM users")
    suspend fun clear()
}