package com.dluvian.voyage.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.cmd.InboxViewAction
import com.dluvian.voyage.cmd.InboxViewAppend
import com.dluvian.voyage.cmd.InboxViewApplyFilter
import com.dluvian.voyage.cmd.InboxViewDismissFilter
import com.dluvian.voyage.cmd.InboxViewInit
import com.dluvian.voyage.cmd.InboxViewOpenFilter
import com.dluvian.voyage.cmd.InboxViewRefresh
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.filterSetting.InboxFeedSetting
import com.dluvian.voyage.filterSetting.PostDetails
import com.dluvian.voyage.paginator.Paginator
import com.dluvian.voyage.preferences.InboxPreferences

class InboxViewModel(
    feedProvider: FeedProvider,
    subCreator: SubscriptionCreator,
    val postDetails: State<PostDetails?>,
    val feedState: LazyListState,
    private val inboxPreferences: InboxPreferences
) : ViewModel() {
    val showFilterMenu: MutableState<Boolean> = mutableStateOf(false)
    val setting: MutableState<InboxFeedSetting> =
        mutableStateOf(inboxPreferences.getInboxFeedSetting())
    val paginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )

    fun handle(action: InboxViewAction) {
        when (action) {
            InboxViewInit -> paginator.init(setting.value)
            InboxViewRefresh -> paginator.refresh()
            InboxViewAppend -> paginator.append()
            InboxViewOpenFilter -> showFilterMenu.value = true
            InboxViewDismissFilter -> showFilterMenu.value = false

            is InboxViewApplyFilter -> if (setting.value != action.setting) {
                inboxPreferences.setInboxFeedSettings(setting = action.setting)
                showFilterMenu.value = false
                setting.value = action.setting
                paginator.reinit(setting = action.setting, showRefreshIndicator = true)
            }
        }
    }
}
