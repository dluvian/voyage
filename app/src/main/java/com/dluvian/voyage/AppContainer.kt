package com.dluvian.voyage

import android.content.Context
import androidx.room.Room
import com.dluvian.voyage.data.room.AppDatabase

class AppContainer(context: Context) {
    val roomDb: AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "voyage_database",
    ).build()
}
