package com.dluvian.voyage.data.room.dao.insert

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dluvian.voyage.data.event.ValidatedLegacyReply
import com.dluvian.voyage.data.room.entity.LegacyReplyEntity

@Dao
interface LegacyReplyInsertDao {
    @Transaction
    suspend fun insertLegacyReplies(replies: Collection<ValidatedLegacyReply>) {
        if (replies.isEmpty()) return

        val entities = replies.map { LegacyReplyEntity.from(validatedLegacyReply = it) }
        internalInsertReplies(replies = entities)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertReplies(replies: Collection<LegacyReplyEntity>)
}
