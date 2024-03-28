package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.LeveledCommentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.CommentDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import rust.nostr.protocol.Nip19Event
import java.util.LinkedList

class ThreadProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val commentDao: CommentDao,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
    private val collapsedIds: Flow<Set<EventIdHex>>
) {

    @OptIn(FlowPreview::class)
    fun getRoot(scope: CoroutineScope, nevent: Nip19Event): Flow<RootPostUI?> {
        scope.launchIO { nostrSubscriber.subVotesAndReplies(nevent = nevent) }

        return combine(
            rootPostDao.getRootPostFlow(id = nevent.eventId().toHex()),
            forcedVotes
        ) { post, votes ->
            post?.mapToRootPostUI(forcedVotes = votes)
        }.debounce(SHORT_DEBOUNCE)
    }

    fun getLeveledComments(
        rootId: EventIdHex,
        parentIds: Set<EventIdHex>
    ): Flow<List<LeveledCommentUI>> {
        val commentFlow = commentDao.getCommentsFlow(parentIds = parentIds + rootId)
            .firstThenDistinctDebounce(DEBOUNCE)
            .onEach {
                nostrSubscriber.subVotesAndReplies(postIds = it.map { comment -> comment.id })
            }

        return combine(
            commentFlow,
            forcedVotes,
            collapsedIds,
        ) { comments, votes, collapsed ->
            val result = LinkedList<LeveledCommentUI>()

            for (comment in comments) {
                val parent = result.find { it.comment.id == comment.parentId }

                if (parent?.isCollapsed == true) continue
                if (parent == null && comment.parentId != rootId) continue

                val leveledComment = comment.mapToLeveledCommentUI(
                    level = parent?.level?.plus(1) ?: 0,
                    forcedVotes = votes,
                    collapsedIds = collapsed,
                    parentIds = parentIds
                )

                if (comment.parentId == rootId) {
                    result.add(leveledComment)
                    continue
                }
                result.add(result.indexOf(parent) + 1, leveledComment)
            }

            result
        }
    }
}
