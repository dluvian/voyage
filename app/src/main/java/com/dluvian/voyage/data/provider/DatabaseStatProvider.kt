package com.dluvian.voyage.data.provider

import com.dluvian.voyage.data.room.dao.CountDao
import kotlinx.coroutines.flow.Flow

class DatabaseStatProvider(private val countDao: CountDao) {
    fun getRootPostCountFlow(): Flow<Int> {
        return countDao.countRootPostsFlow()
    }
}
