package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.ThreadViewAction
import com.dluvian.voyage.core.ThreadViewRefresh
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.model.LeveledReplyUI
import com.dluvian.voyage.core.model.ParentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.interactor.ThreadCollapser
import com.dluvian.voyage.data.provider.ThreadProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import rust.nostr.protocol.Nip19Event

class ThreadViewModel(
    private val threadProvider: ThreadProvider,
    private val threadCollapser: ThreadCollapser,
    val threadState: LazyListState,
) : ViewModel() {
    val isRefreshing = mutableStateOf(false)
    var parentIsAvailable: StateFlow<Boolean> = MutableStateFlow(false)
    var localRoot: StateFlow<ParentUI?> = MutableStateFlow(null)
    val leveledReplies: MutableState<StateFlow<List<LeveledReplyUI>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    val totalReplyCount: MutableState<StateFlow<Int>> = mutableStateOf(MutableStateFlow(0))
    private val parentIds = mutableStateOf(emptySet<EventIdHex>())
    private var nevent: Nip19Event? = null

    fun openThread(nevent: Nip19Event, parentUi: ParentUI?) {
        val id = nevent.eventId().toHex()
        if (id == localRoot.value?.id) return

        leveledReplies.value = MutableStateFlow(emptyList())
        this.nevent = nevent

        localRoot = threadProvider
            .getLocalRoot(scope = viewModelScope, nevent = nevent, isInit = true)
            .stateIn(viewModelScope, SharingStarted.Eagerly, parentUi)
        checkParentAvailability(replyId = id, parentUi = parentUi)
        loadReplies(
            rootId = id,
            parentId = id,
            isInit = true,
        )
    }

    fun handle(action: ThreadViewAction) {
        when (action) {
            is ThreadViewRefresh -> refresh()
            is ThreadViewToggleCollapse -> threadCollapser.toggleCollapse(id = action.id)
            is ThreadViewShowReplies -> loadReplies(
                rootId = localRoot.value?.getRelevantId(),
                parentId = action.id,
                isInit = false,
            )
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return

        val currentNevent = nevent ?: return
        val currentRoot = localRoot.value

        isRefreshing.value = true

        viewModelScope.launchIO {
            localRoot = threadProvider
                .getLocalRoot(scope = viewModelScope, nevent = currentNevent, isInit = false)
                .stateIn(viewModelScope, SharingStarted.Eagerly, currentRoot)
            parentIsAvailable = threadProvider
                .getParentIsAvailableFlow(
                    scope = viewModelScope,
                    replyId = currentNevent.eventId().toHex()
                )
                .stateIn(viewModelScope, SharingStarted.Eagerly, parentIsAvailable.value)
            leveledReplies.value = threadProvider.getLeveledReplies(
                rootId = currentNevent.eventId().toHex(),
                parentIds = parentIds.value,
            )
                .stateIn(
                    viewModelScope,
                    SharingStarted.Eagerly,
                    leveledReplies.value.value
                )
            delay(DELAY_1SEC)
        }.invokeOnCompletion { isRefreshing.value = false }
    }

    private fun loadReplies(
        rootId: EventIdHex?,
        parentId: EventIdHex,
        isInit: Boolean,
    ) {
        if (rootId == null) return

        val init = if (isInit) emptyList() else leveledReplies.value.value
        parentIds.value += rootId
        parentIds.value += parentId
        totalReplyCount.value = threadProvider.getTotalReplyCount(rootId = rootId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
        leveledReplies.value = threadProvider
            .getLeveledReplies(rootId = rootId, parentIds = parentIds.value)
            .stateIn(viewModelScope, SharingStarted.Eagerly, init)
    }

    private fun checkParentAvailability(replyId: EventIdHex, parentUi: ParentUI?) {
        if (parentUi is RootPostUI) return

        parentIsAvailable = threadProvider
            .getParentIsAvailableFlow(scope = viewModelScope, replyId = replyId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    }
}
