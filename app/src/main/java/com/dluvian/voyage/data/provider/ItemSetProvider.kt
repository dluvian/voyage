package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.data.model.ItemSetMeta
import com.dluvian.voyage.data.room.dao.ItemSetDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ItemSetProvider(private val itemSetDao: ItemSetDao) {
    fun getMySetsFlow(): Flow<List<ItemSetMeta>> {
        return combine(
            itemSetDao.getMyProfileSetMetasFlow().firstThenDistinctDebounce(SHORT_DEBOUNCE),
            itemSetDao.getMyTopicSetMetasFlow().firstThenDistinctDebounce(SHORT_DEBOUNCE)
        ) { profileSets, topicSets ->
            profileSets.plus(topicSets).distinctBy { it.identifier }
        }
    }
}
