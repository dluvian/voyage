package com.dluvian.voyage.data.provider

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.model.CommentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.interactor.Vote
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.room.dao.CommentDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import rust.nostr.protocol.Nip19Event

class ThreadProvider(
    private val nostrSubscriber: NostrSubscriber,
    private val rootPostDao: RootPostDao,
    private val commentDao: CommentDao,
    private val forcedVotes: Flow<Map<EventIdHex, Vote>>,
) {

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
    fun getReplies(parentIds: Collection<EventIdHex>): Flow<Map<EventIdHex, List<CommentUI>>> {
        if (parentIds.isEmpty()) return flowOf(emptyMap())
        return combine(
            commentDao.getCommentsFlow(parentIds = parentIds),
            forcedVotes
        ) { comments, votes ->
            comments.map { it.mapToCommentUI(forcedVotes = votes) }
        }.debounce(SHORT_DEBOUNCE)
            .map { commentUIs -> commentUIs.groupBy { it.parentId } }
    }
}
