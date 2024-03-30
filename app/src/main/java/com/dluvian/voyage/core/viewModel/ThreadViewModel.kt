package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.createNevent
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.ThreadViewAction
import com.dluvian.voyage.core.ThreadViewRefresh
import com.dluvian.voyage.core.ThreadViewShowReplies
import com.dluvian.voyage.core.ThreadViewToggleCollapse
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.LeveledCommentUI
import com.dluvian.voyage.core.model.RootPostUI
import com.dluvian.voyage.data.interactor.ThreadCollapser
import com.dluvian.voyage.data.provider.ThreadProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ThreadViewModel(
    private val threadProvider: ThreadProvider,
    private val threadCollapser: ThreadCollapser,
) : ViewModel() {
    val isRefreshing = mutableStateOf(false)
    var root: StateFlow<RootPostUI?> = MutableStateFlow(null)
    val leveledComments: MutableState<StateFlow<List<LeveledCommentUI>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    private val parentIds = mutableStateOf(emptySet<EventIdHex>())

    fun openThread(rootPost: RootPostUI) {
        if (rootPost.id == root.value?.id) return

        leveledComments.value = MutableStateFlow(emptyList())
        parentIds.value = setOf()
        root =
            threadProvider.getRoot(scope = viewModelScope, nevent = createNevent(hex = rootPost.id))
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), rootPost)
        loadReplies(rootId = rootPost.id, parentId = rootPost.id, isInit = true)
    }

    fun handle(action: ThreadViewAction) {
        when (action) {
            is ThreadViewRefresh -> refresh()
            is ThreadViewToggleCollapse -> threadCollapser.toggleCollapse(id = action.id)
            is ThreadViewShowReplies -> loadReplies(
                rootId = root.value?.id,
                parentId = action.id,
                isInit = false
            )
        }
    }

    private fun refresh() {
        if (isRefreshing.value) return
        val currentRoot = root.value ?: return

        isRefreshing.value = true

        viewModelScope.launchIO {
            val nip19 = createNevent(hex = currentRoot.id)

            root = threadProvider.getRoot(scope = viewModelScope, nevent = nip19)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), currentRoot)
            leveledComments.value = threadProvider.getLeveledComments(
                rootId = currentRoot.id,
                parentIds = parentIds.value
            )
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    leveledComments.value.value
                )
            delay(DELAY_1SEC)
        }.invokeOnCompletion { isRefreshing.value = false }
    }

    private fun loadReplies(rootId: EventIdHex?, parentId: EventIdHex, isInit: Boolean) {
        if (rootId == null || parentIds.value.contains(parentId)) return

        val init = if (isInit) emptyList() else leveledComments.value.value
        parentIds.value += parentId
        leveledComments.value =
            threadProvider.getLeveledComments(rootId = rootId, parentIds = parentIds.value)
                .stateIn(viewModelScope, SharingStarted.Eagerly, init)
    }
}
