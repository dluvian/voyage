package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.ReplyDao
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
    private val replyDao: ReplyDao,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
    private val collapsedIds: Flow<Set<EventIdHex>>,
    private val annotatedStringProvider: AnnotatedStringProvider,
    private val nameCache: MutableMap<PubkeyHex, String?>,
) {

    @OptIn(FlowPreview::class)
    fun getRoot(scope: CoroutineScope, nevent: Nip19Event): Flow<RootPostUI?> {
        scope.launchIO { nostrSubscriber.subVotesAndReplies(nevent = nevent) }

        return combine(
            rootPostDao.getRootPostFlow(id = nevent.eventId().toHex()),
            forcedVotes
        ) { post, votes ->
            post?.mapToRootPostUI(
                forcedVotes = votes,
                annotatedStringProvider = annotatedStringProvider
            )
        }.debounce(SHORT_DEBOUNCE)
    }

    fun getLeveledReplies(
        rootId: EventIdHex,
        parentIds: Set<EventIdHex>
    ): Flow<List<LeveledReplyUI>> {
        val replyFlow = replyDao.getReplyFlow(parentIds = parentIds + rootId)
            .firstThenDistinctDebounce(DEBOUNCE)
            .onEach {
                nostrSubscriber.subVotesAndReplies(postIds = it.map { reply -> reply.id })
            }

        return combine(
            replyFlow,
            forcedVotes,
            collapsedIds,
        ) { replies, votes, collapsed ->
            val result = LinkedList<LeveledReplyUI>()

            for (reply in replies) {
                if (!reply.authorName.isNullOrEmpty()) {
                    nameCache.putIfAbsent(reply.pubkey, reply.authorName)
                }
                val parent = result.find { it.reply.id == reply.parentId }

                if (parent?.isCollapsed == true) continue
                if (parent == null && reply.parentId != rootId) continue

                val leveledReply = reply.mapToLeveledReplyUI(
                    level = parent?.level?.plus(1) ?: 0,
                    forcedVotes = votes,
                    collapsedIds = collapsed,
                    parentIds = parentIds,
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
    }
}
