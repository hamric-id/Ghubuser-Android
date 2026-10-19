package com.hamric.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hamric.core.database.entity.DetailUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetailUserDao {
    @Query("SELECT * FROM details_users WHERE login = :login LIMIT 1")
    fun observe(login:String): Flow<DetailUserEntity?>

    @Query("SELECT * FROM details_users WHERE login = :login LIMIT 1")
    suspend fun get(login: String): DetailUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DetailUserEntity)

    @Query("UPDATE details_users SET lastAccessedAt = :timestamp WHERE login = :login")
    suspend fun touch(login: String, timestamp: Long)

    @Query(
        """ 
        DELETE FROM details_users 
        WHERE login NOT IN(
            SELECT login FROM details_users
            ORDER BY lastAccessedAt DESC
            LIMIT :maxEntries
        )
        """
    )
    suspend fun trimTo(maxEntries: Int)
}