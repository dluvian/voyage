package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.model.CommentUI
import com.dluvian.voyage.core.model.ThreadUI
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
    @OptIn(FlowPreview::class)
    fun getThread(nip19Event: Nip19Event): Flow<ThreadUI> {
        nostrSubscriber.subVotesAndReplies(nip19Event = nip19Event)
        val rootId = nip19Event.eventId().toHex()
        val postFlow = rootPostDao.getRootPostFlow(id = rootId)
        val commentsFlow = commentDao.getCommentsFlow(parentId = rootId)

        return combine(
            postFlow,
            commentsFlow,
            forcedVotes,
            collapsedIds
        ) { post, comments, votes, ids ->
            ThreadUI(
                rootPost = post?.mapToRootPostUI(forcedVotes = votes),
                comments = comments.map {
                    it.mapToCommentUI(
                        forcedVotes = votes,
                        collapsedIds = ids
                    )
                }
            )
        }
            .debounce(SHORT_DEBOUNCE)
            .onEach { treadUI ->
                nostrSubscriber.subVotesAndReplies(postIds = treadUI.comments.map { it.id })
            }
    }

    fun getComments(parentId: EventIdHex): Flow<List<CommentUI>> {
        TODO()
    }
}
