package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.RESUB_TIMEOUT
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.nostr.SubBatcher
import com.dluvian.voyage.data.room.dao.ReplyDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import com.dluvian.voyage.data.room.view.ReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import rust.nostr.protocol.Nip19Event
import java.util.Collections
import java.util.LinkedList

class ThreadProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val replyDao: ReplyDao,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
    private val collapsedIds: Flow<Set<EventIdHex>>,
    private val annotatedStringProvider: AnnotatedStringProvider,
    private val nameCache: MutableMap<PubkeyHex, String?>,
    private val subBatcher: SubBatcher,
    private val relayProvider: RelayProvider,
    private val oldestUsedEvent: OldestUsedEvent,
) {

    fun getRoot(scope: CoroutineScope, nevent: Nip19Event): Flow<RootPostUI?> {
        scope.launchIO { nostrSubscriber.subVotesAndReplies(nevent = nevent) }

        return combine(
            rootPostDao.getRootPostFlow(id = nevent.eventId().toHex())
                .firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes
        ) { post, votes ->
            handleProfileSub(post = post)
            post?.mapToRootPostUI(
                forcedVotes = votes,
                annotatedStringProvider = annotatedStringProvider
            )
        }.onEach {
            oldestUsedEvent.updateOldestCreatedAt(it?.createdAt)
        }
    }

    // Don't update oldestCreatedAt in replies. They are always younger than root
    fun getLeveledReplies(
        rootId: EventIdHex,
        parentIds: Set<EventIdHex>
    ): Flow<List<LeveledReplyUI>> {
        val replyFlow = replyDao.getReplyFlow(parentIds = parentIds + rootId)
            .firstThenDistinctDebounce(DEBOUNCE)
            .onEach { handleProfileSub(replies = it) }

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
            .onEach { nostrSubscriber.subVotesAndReplies(posts = it.map { reply -> reply.reply }) }
    }

    private val pubkeyCache = Collections.synchronizedSet(mutableSetOf<EventIdHex>())
    private var lastUpdate = System.currentTimeMillis()
    private fun handleProfileSub(post: RootPostView?) {
        if (post == null || !post.authorName.isNullOrEmpty()) return
        if (pubkeyCache.contains(post.pubkey)) return

        pubkeyCache.add(post.pubkey)
        relayProvider.getReadRelays(includeConnected = true).forEach { relay ->
            subBatcher.submitProfile(relayUrl = relay, pubkey = post.pubkey)
        }
        handleLastUpdate()
    }

    private fun handleProfileSub(replies: List<ReplyView>) {
        if (replies.isEmpty()) return

        val unknownPubkeys = replies.filter { it.authorName.isNullOrEmpty() }
            .map { it.pubkey }
            .distinct()
        if (unknownPubkeys.isEmpty()) return

        val toSub: List<PubkeyHex>
        synchronized(pubkeyCache) {
            toSub = unknownPubkeys - pubkeyCache
            if (toSub.isEmpty()) return
            pubkeyCache.addAll(toSub)
        }

        relayProvider.getReadRelays(includeConnected = true).forEach { relay ->
            subBatcher.submitProfiles(relayUrl = relay, pubkeys = toSub)
        }
        handleLastUpdate()
    }

    private fun handleLastUpdate() {
        val currentMillis = System.currentTimeMillis()
        if (currentMillis - lastUpdate > RESUB_TIMEOUT) {
            pubkeyCache.clear()
            lastUpdate = currentMillis
        }
    }
}
