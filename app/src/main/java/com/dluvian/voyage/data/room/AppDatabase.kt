package com.dluvian.voyage.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dluvian.voyage.data.room.dao.PostDao
import com.dluvian.voyage.data.room.entity.PostEntity

@Database(
    version = 1,
    exportSchema = true,
    autoMigrations = [
    ],
    entities = [
        PostEntity::class,
    ],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}
