package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.filterSetting.InboxFeedSetting
import com.dluvian.voyage.model.InboxViewAppend
import com.dluvian.voyage.model.InboxViewApplyFilter
import com.dluvian.voyage.model.InboxViewCmd
import com.dluvian.voyage.model.InboxViewDismissFilter
import com.dluvian.voyage.model.InboxViewOpen
import com.dluvian.voyage.model.InboxViewOpenFilter
import com.dluvian.voyage.model.InboxViewRefresh
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.preferences.InboxPreferences
import com.dluvian.voyage.provider.FeedProvider
import com.dluvian.voyage.provider.IEventUpdate
import kotlinx.coroutines.launch
import rust.nostr.sdk.Event
import java.util.concurrent.atomic.AtomicBoolean

class InboxViewModel(
    feedProvider: FeedProvider,
    val feedState: LazyListState,
    private val inboxPreferences: InboxPreferences,
) : ViewModel(), IEventUpdate {
    private val isInitialized = AtomicBoolean(false)
    val showFilterMenu: MutableState<Boolean> = mutableStateOf(false)
    val setting: MutableState<InboxFeedSetting> =
        mutableStateOf(inboxPreferences.getInboxFeedSetting())
    val paginator = Paginator(feedProvider)

    init {
        paginator.initSetting(setting.value)
    }

    fun handle(cmd: InboxViewCmd) {
        when (cmd) {
            InboxViewOpen -> {
                if (isInitialized.compareAndSet(false, true)) {
                    viewModelScope.launch {
                        paginator.refresh()
                    }
                    return
                }
                viewModelScope.launch {
                    paginator.dbRefreshInPlace()
                }
            }

            InboxViewRefresh -> viewModelScope.launch {
                paginator.refresh()
            }

            InboxViewAppend -> viewModelScope.launch {
                paginator.nextPage()
            }
            InboxViewOpenFilter -> showFilterMenu.value = true
            InboxViewDismissFilter -> showFilterMenu.value = false

            is InboxViewApplyFilter -> if (setting.value != cmd.setting) {
                inboxPreferences.setInboxFeedSettings(setting = cmd.setting)
                showFilterMenu.value = false
                setting.value = cmd.setting
                paginator.initSetting(setting = cmd.setting)
                viewModelScope.launch {
                    paginator.refresh()
                }
            }
        }
    }

    override suspend fun update(event: Event) {
        paginator.update(event)
    }
}
