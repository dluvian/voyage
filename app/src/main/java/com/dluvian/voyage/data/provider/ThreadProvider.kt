package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.model.FeedItemUI
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.utils.containsNoneIgnoreCase
import com.dluvian.voyage.core.utils.firstThenDistinctDebounce
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.model.SingularPubkey
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.nostr.createNevent
import com.dluvian.voyage.data.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import rust.nostr.protocol.Nip19Event
import java.util.LinkedList

private const val TAG = "ThreadProvider"

class ThreadProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
    private val room: AppDatabase,
    private val collapsedIds: Flow<Set<EventIdHex>>,
    private val annotatedStringProvider: AnnotatedStringProvider,
    private val oldestUsedEvent: OldestUsedEvent,
    private val forcedVotes: Flow<Map<EventIdHex, Boolean>>,
    private val forcedFollows: Flow<Map<PubkeyHex, Boolean>>,
    private val forcedBookmarks: Flow<Map<EventIdHex, Boolean>>,
    private val muteProvider: MuteProvider,
) {

    fun getLocalRoot(
        scope: CoroutineScope,
        nevent: Nip19Event,
        isInit: Boolean
    ): Flow<FeedItemUI?> {
        val id = nevent.eventId().toHex()
        scope.launchIO {
            if (!room.existsDao().postExists(id = id)) {
                nostrSubscriber.subPost(nevent = nevent)
                delay(DELAY_1SEC)
            }
            val author = nevent.author()?.toHex() ?: room.postDao().getAuthor(id = id)
            if (author != null) {
                lazyNostrSubscriber.lazySubUnknownProfiles(
                    selection = SingularPubkey(pubkey = author)
                )
            }

            if (isInit) lazyNostrSubscriber.lazySubRepliesAndVotes(parentId = id)
            else nostrSubscriber.subVotesAndReplies(parentIds = listOf(id))
        }

        val rootFlow = room.rootPostDao().getRootPostFlow(id = id)
        val replyFlow = room.replyDao().getReplyFlow(id = id)

        return combine(
            rootFlow.firstThenDistinctDebounce(SHORT_DEBOUNCE),
            replyFlow.firstThenDistinctDebounce(SHORT_DEBOUNCE),
            forcedVotes,
            forcedFollows,
            forcedBookmarks,
        ) { post, reply, votes, follows, bookmarks ->
            post?.mapToRootPostUI(
                forcedVotes = votes,
                forcedFollows = follows,
                forcedBookmarks = bookmarks,
                annotatedStringProvider = annotatedStringProvider
            ) ?: reply?.mapToLegacyReplyUI(
                forcedFollows = follows,
                forcedVotes = votes,
                forcedBookmarks = bookmarks,
                annotatedStringProvider = annotatedStringProvider
            )
        }.onEach {
            oldestUsedEvent.updateOldestCreatedAt(it?.createdAt)
        }
    }

    fun getParentIsAvailableFlow(scope: CoroutineScope, replyId: EventIdHex): Flow<Boolean> {
        scope.launchIO {
            val parentId = room.replyDao().getParentId(id = replyId) ?: return@launchIO
            if (!room.existsDao().postExists(id = parentId)) {
                Log.i(TAG, "Parent $parentId is not available yet. Subscribing to it")
                nostrSubscriber.subPost(nevent = createNevent(hex = parentId))
            }
        }

        return room.existsDao().parentExistsFlow(replyId = replyId)
    }

    // Unfiltered count for ProgressBar purpose
    fun getTotalReplyCount(rootId: EventIdHex): Flow<Int> {
        return room.replyDao().getReplyCountFlow(parentId = rootId)
            .firstThenDistinctDebounce(SHORT_DEBOUNCE)
    }

    // Don't update oldestCreatedAt in replies. They are always younger than root
    fun getLeveledReplies(
        rootId: EventIdHex,
        parentIds: Set<EventIdHex>,
    ): Flow<List<LeveledReplyUI>> {
        val replyFlow = room.replyDao().getRepliesFlow(parentIds = parentIds + rootId)
            .firstThenDistinctDebounce(DEBOUNCE)
        val mutedWords = muteProvider.getMutedWords()

        return combine(
            replyFlow,
            forcedVotes,
            forcedFollows,
            forcedBookmarks,
            collapsedIds,
        ) { replies, votes, follows, bookmarks, collapsed ->
            val filteredReplies = replies.filter { reply ->
                reply.content.containsNoneIgnoreCase(strs = mutedWords)
            }

            val result = LinkedList<LeveledReplyUI>()

            for (reply in filteredReplies) {
                val parent = result.find { it.reply.id == reply.parentId }

                if (parent?.isCollapsed == true) continue
                if (parent == null && reply.parentId != rootId) continue

                val leveledReply = reply.mapToLeveledReplyUI(
                    level = parent?.level?.plus(1) ?: 0,
                    forcedVotes = votes,
                    forcedFollows = follows,
                    collapsedIds = collapsed,
                    parentIds = parentIds,
                    forcedBookmarks = bookmarks,
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
            .onEach {
                nostrSubscriber.subVotesAndReplies(
                    parentIds = it.map { reply -> reply.reply.getRelevantId() }
                )
                nostrSubscriber.subProfiles(
                    pubkeys = it.filter { reply -> reply.reply.authorName.isNullOrEmpty() }
                        .map { reply -> reply.reply.pubkey }
                )
            }
    }
}
