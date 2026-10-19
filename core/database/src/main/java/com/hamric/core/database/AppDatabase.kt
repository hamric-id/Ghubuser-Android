package com.hamric.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hamric.core.database.dao.DetailUserDao
import com.hamric.core.database.dao.PaginationDao
import com.hamric.core.database.dao.UsersDao
import com.hamric.core.database.entity.DetailUserEntity
import com.hamric.core.database.entity.PaginationEntity
import com.hamric.core.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PaginationEntity::class,
        DetailUserEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase(){
    abstract fun userDao(): UsersDao
    abstract fun paginationDao(): PaginationDao
    abstract fun detailUserDao(): DetailUserDao
}