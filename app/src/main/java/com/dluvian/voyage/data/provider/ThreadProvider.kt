package com.dluvian.voyage.data.provider

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.model.CommentUI
import com.dluvian.voyage.core.model.ThreadUI
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.CommentDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import com.dluvian.voyage.data.room.view.CommentView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rust.nostr.protocol.Nip19Event

class ThreadProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val commentDao: CommentDao,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
    private val collapsedIds: Flow<Set<EventIdHex>>
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val replyMap = MutableStateFlow(mapOf<EventIdHex, StateFlow<List<CommentUI>>>())

    fun getThread(nip19Event: Nip19Event): Flow<ThreadUI> {
        replyMap.update { emptyMap() }
        nostrSubscriber.subVotesAndReplies(nip19Event = nip19Event)
        val rootId = nip19Event.eventId().toHex()
        val postFlow = rootPostDao.getRootPostFlow(id = rootId)
        val commentFlow = mapCommentUIFlow(
            commentFlow = commentDao.getCommentsFlow(parentId = rootId)
        )

        return combine(postFlow, commentFlow, forcedVotes, replyMap) { post, comments, votes, _ ->
            ThreadUI(
                rootPost = post?.mapToRootPostUI(forcedVotes = votes),
                comments = comments
            )
        }.onEach {
            Log.i(
                "LOLOL",
                replyMap.value.mapValues { it2 -> it2.value.value.size }.toString()
            )
        }
    }

    fun loadReplies(parentId: EventIdHex) {
        if (replyMap.value.containsKey(parentId)) return
        scope.launch {
            val commentFlow = commentDao.getCommentsFlow(parentId = parentId)
            val finalFlow = mapCommentUIFlow(commentFlow = commentFlow)
                .onEach { replyMap.update { it } }
                .stateIn(scope, SharingStarted.Eagerly, emptyList())

            replyMap.update {
                val mutable = it.toMutableMap()
                mutable[parentId] = finalFlow
                mutable
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun mapCommentUIFlow(commentFlow: Flow<List<CommentView>>): Flow<List<CommentUI>> {
        return combine(
            commentFlow,
            forcedVotes,
            collapsedIds,
            replyMap
        ) { comments, votes, collapsed, replies ->
            comments.map {
                it.mapToCommentUI(
                    forcedVotes = votes,
                    collapsedIds = collapsed,
                    replies = replies[it.id]?.value.orEmpty()
                )
            }
        }
            .debounce(SHORT_DEBOUNCE)
            .onEach { comments ->
                nostrSubscriber.subVotesAndReplies(postIds = comments.map { it.id })
            }
    }
}
