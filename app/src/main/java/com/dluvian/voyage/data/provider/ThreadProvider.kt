package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.ReplyDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import rust.nostr.protocol.Nip19Event
import java.util.LinkedList

class ThreadProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val replyDao: ReplyDao,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
    private val collapsedIds: Flow<Set<EventIdHex>>,
    private val annotatedStringProvider: AnnotatedStringProvider,
    private val oldestUsedEvent: OldestUsedEvent,
    private val forcedFollows: Flow<Map<PubkeyHex, Boolean>>,
) {

    fun getRoot(scope: CoroutineScope, nevent: Nip19Event): Flow<RootPostUI?> {
        scope.launchIO { nostrSubscriber.subVotesAndReplies(nevent = nevent) }

        return combine(
            rootPostDao.getRootPostFlow(id = nevent.eventId().toHex())
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows,
        ) { post, votes, follows ->
            post?.mapToRootPostUI(
                forcedVotes = votes,
                forcedFollows = follows,
                annotatedStringProvider = annotatedStringProvider
            )
        }.onEach {
            oldestUsedEvent.updateOldestCreatedAt(it?.createdAt)
        }
    }

    // Don't update oldestCreatedAt in replies. They are always younger than root
    fun getLeveledReplies(
        rootId: EventIdHex,
        parentIds: Set<EventIdHex>,
        opPubkey: PubkeyHex?
    ): Flow<List<LeveledReplyUI>> {
        val replyFlow = replyDao.getReplyFlow(parentIds = parentIds + rootId)
            .firstThenDistinctDebounce(DEBOUNCE)

        return combine(
            replyFlow,
            forcedVotes,
            forcedFollows,
            collapsedIds,
        ) { replies, votes, follows, collapsed ->
            val result = LinkedList<LeveledReplyUI>()

            for (reply in replies) {
                val parent = result.find { it.reply.id == reply.parentId }

                if (parent?.isCollapsed == true) continue
                if (parent == null && reply.parentId != rootId) continue

                val leveledReply = reply.mapToLeveledReplyUI(
                    level = parent?.level?.plus(1) ?: 0,
                    forcedVotes = votes,
                    forcedFollows = follows,
                    collapsedIds = collapsed,
                    parentIds = parentIds,
                    isOp = opPubkey == reply.pubkey,
                    annotatedStringProvider = annotatedStringProvider
                )

                if (reply.parentId == rootId) {
                    result.add(leveledReply)
                    continue
                }
                result.add(result.indexOf(parent) + 1, leveledReply)
            }

            result
        }
            .onEach { nostrSubscriber.subVotesAndReplies(posts = it.map { reply -> reply.reply }) }
    }
}
