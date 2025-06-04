package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.model.ThreadReplyCtx
import com.dluvian.voyage.model.ThreadRootCtx
import com.dluvian.voyage.model.ThreadViewCmd
import com.dluvian.voyage.model.ThreadViewPopNevent
import com.dluvian.voyage.model.ThreadViewPopUIEvent
import com.dluvian.voyage.model.ThreadViewPushNevent
import com.dluvian.voyage.model.ThreadViewPushUIEvent
import com.dluvian.voyage.model.ThreadViewRefresh
import com.dluvian.voyage.model.ThreadViewShowReplies
import com.dluvian.voyage.model.ThreadViewToggleCollapse
import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.provider.IEventUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId

class ThreadViewModel(
    val threadState: LazyListState,
    private val service: NostrService
) : ViewModel(), IEventUpdate {
    val isRefreshing = mutableStateOf(false)
    val parentIsAvailable = mutableStateOf(false)
    val root = mutableStateOf<ThreadRootCtx?>(null)
    val replies = mutableStateOf(emptyList<ThreadReplyCtx>())

    private val mutex = Mutex()
    private val collapsedIds = mutableSetOf<EventId>()

    fun handle(cmd: ThreadViewCmd) {
        when (cmd) {
            is ThreadViewPushNevent -> TODO()
            is ThreadViewPushUIEvent -> TODO()
            is ThreadViewPopNevent -> TODO()
            is ThreadViewPopUIEvent -> TODO()
            is ThreadViewToggleCollapse -> viewModelScope.launch {
                mutex.withLock {
                    val alreadyInSet = collapsedIds.add(cmd.id)
                    if (alreadyInSet) collapsedIds.remove(cmd.id)
                }
            }

            ThreadViewRefresh -> TODO()
            is ThreadViewShowReplies -> TODO()
        }
    }

    override suspend fun update(event: Event) {
        TODO("Contactslists, profiles, replies")
    }
}
