package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.model.LeveledCommentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.CommentDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import rust.nostr.protocol.Nip19Event

class ThreadProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val commentDao: CommentDao,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
    private val collapsedIds: Flow<Set<EventIdHex>>
) {
    private val tag = "ThreadProvider"

    @OptIn(FlowPreview::class)
    fun getRoot(nip19Event: Nip19Event): Flow<RootPostUI?> {
        nostrSubscriber.subVotesAndReplies(nip19Event = nip19Event)
        val rootId = nip19Event.eventId().toHex()
        val postFlow = rootPostDao.getRootPostFlow(id = rootId)

        return combine(postFlow, forcedVotes) { post, votes ->
            post?.mapToRootPostUI(forcedVotes = votes)
        }.debounce(SHORT_DEBOUNCE)
    }

    @OptIn(FlowPreview::class)
    fun getLeveledComments(
        rootId: EventIdHex,
        parentIds: Set<EventIdHex>
    ): Flow<List<LeveledCommentUI>> {
        return combine(
            commentDao.getCommentsFlow(parentIds = parentIds + rootId),
            forcedVotes,
            collapsedIds,
        ) { comments, votes, collapsed ->
            val result = mutableListOf<LeveledCommentUI>()

            for (comment in comments) {
                val parent = result.find { it.comment.id == comment.parentId }

                val leveledComment = comment.mapToLeveledCommentUI(
                    level = parent?.level?.plus(1) ?: 0,
                    forcedVotes = votes,
                    collapsedIds = collapsed,
                    parentIds = parentIds
                )
                if (parent == null) {
                    if (comment.parentId == rootId) result.add(leveledComment)
                    else Log.w(tag, "Comment ${comment.id} is out of order and discarded")
                    continue
                }

                result.add(result.indexOf(parent) + 1, leveledComment)
            }

            result
        }
            .debounce(SHORT_DEBOUNCE)
            .onEach {
                nostrSubscriber.subVotesAndReplies(postIds = it.map { comment -> comment.comment.id })
            }
    }
}
