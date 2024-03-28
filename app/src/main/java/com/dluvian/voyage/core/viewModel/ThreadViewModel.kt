package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.createEmptyNip19Event
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.ThreadViewAction
import com.dluvian.voyage.core.ThreadViewRefresh
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.model.CommentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.navigator.ThreadNavView
import com.dluvian.voyage.data.interactor.ThreadCollapser
import com.dluvian.voyage.data.provider.ThreadProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rust.nostr.protocol.EventId

class ThreadViewModel(
    private val threadProvider: ThreadProvider,
    private val threadCollapser: ThreadCollapser,
) : ViewModel() {
    val collapsedIds = threadCollapser.collapsedIds
    var root: StateFlow<RootPostUI?> = MutableStateFlow(null)
    val isRefreshing = mutableStateOf(false)
    var allReplies: MutableState<StateFlow<Map<EventIdHex, List<CommentUI>>>> =
        mutableStateOf(MutableStateFlow(emptyMap()))
    private val parentIds = mutableStateOf(emptySet<EventIdHex>())


    fun openThread(threadNavView: ThreadNavView) {
        val id = threadNavView.nip19Event.eventId().toHex()
        val isSame = id == root.value?.id
        if (!isSame) parentIds.value = setOf()
        val initVal = if (isSame) root.value else null

        root = threadProvider.getRoot(nip19Event = threadNavView.nip19Event)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initVal)
        loadReplies(parentId = id, isInit = true)
    }

    fun handle(action: ThreadViewAction) {
        when (action) {
            is ThreadViewRefresh -> refresh()
            is ThreadViewToggleCollapse -> threadCollapser.toggleCollapse(id = action.id)
            // TODO: Give reply Map to UI, call loadReplies with all expanded parentIds
            is ThreadViewShowReplies -> loadReplies(parentId = action.id, isInit = false)
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return
        val currentRoot = root.value ?: return

        isRefreshing.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val nip19 = createEmptyNip19Event(eventId = EventId.fromHex(currentRoot.id))
            root = threadProvider.getRoot(nip19Event = nip19)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), currentRoot)
            allReplies.value = threadProvider.getReplies(parentIds = parentIds.value)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), allReplies.value.value)
            delay(DELAY_1SEC)
        }.invokeOnCompletion { isRefreshing.value = false }
    }

    private fun loadReplies(parentId: EventIdHex, isInit: Boolean) {
        if (parentIds.value.contains(parentId)) return

        val init = if (isInit) {
            emptyMap()
        } else allReplies.value.value

        parentIds.value += parentId
        allReplies.value = threadProvider.getReplies(parentIds = parentIds.value)
            .stateIn(viewModelScope, SharingStarted.Eagerly, init)
    }
}
