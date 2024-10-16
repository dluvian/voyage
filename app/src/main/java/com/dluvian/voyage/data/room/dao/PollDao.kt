package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.data.room.view.PollOptionView
import com.dluvian.voyage.data.room.view.PollView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface PollDao {
    fun getFullPollFlow(pollId: EventIdHex): Flow<Pair<PollView, List<PollOptionView>>?> {
        return combine(
            internalGetPollFlow(pollId = pollId),
            internalGetPollOptionsFlow(pollId = pollId)
        ) { poll, options ->
            poll?.let { Pair(it, options) }
        }
    }

    @Query("SELECT * FROM PollView WHERE id = :pollId")
    fun internalGetPollFlow(pollId: EventIdHex): Flow<PollView?>

    @Query("SELECT * FROM PollOptionView WHERE pollId = :pollId")
    fun internalGetPollOptionsFlow(pollId: EventIdHex): Flow<List<PollOptionView>>
}