package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.InboxViewAction
import com.dluvian.voyage.core.InboxViewAppend
import com.dluvian.voyage.core.InboxViewApplyFilter
import com.dluvian.voyage.core.InboxViewDismissFilter
import com.dluvian.voyage.core.InboxViewInit
import com.dluvian.voyage.core.InboxViewOpenFilter
import com.dluvian.voyage.core.InboxViewRefresh
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.PostDetails
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.preferences.InboxPreferences
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.MuteProvider

class InboxViewModel(
    feedProvider: FeedProvider,
    muteProvider: MuteProvider,
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
        muteProvider = muteProvider,
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
