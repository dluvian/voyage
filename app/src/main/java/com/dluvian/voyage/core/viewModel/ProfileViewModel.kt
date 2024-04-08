package com.dluvian.voyage.core.viewModel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.nostr_kt.createNprofile
import com.dluvian.voyage.core.ProfileViewAction
import com.dluvian.voyage.core.ProfileViewAppend
import com.dluvian.voyage.core.ProfileViewRefresh
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.data.model.FullProfileUI
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ProfileProvider
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    feedProvider: FeedProvider,
    private val subCreator: SubscriptionCreator,
    private val profileProvider: ProfileProvider,
    val feedState: LazyListState,
) : ViewModel() {
    val paginator = Paginator(
        feedProvider = feedProvider,
        scope = viewModelScope,
        subCreator = subCreator
    )
    val profile: MutableState<StateFlow<FullProfileUI>> =
        mutableStateOf(MutableStateFlow(FullProfileUI()))

    fun openProfile(profileNavView: ProfileNavView) {
        val pubkeyHex = profileNavView.nprofile.publicKey().toHex()
        if (profile.value.value.inner.pubkey == pubkeyHex) return

        subCreator.unsubAll()
        profile.value = profileProvider.getProfileFlow(nprofile = profileNavView.nprofile)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                FullProfileUI(inner = AdvancedProfileView(pubkey = pubkeyHex))
            )
        paginator.init(setting = ProfileFeedSetting(pubkey = pubkeyHex))
    }

    fun handle(action: ProfileViewAction) {
        when (action) {
            is ProfileViewRefresh -> refresh()
            is ProfileViewAppend -> paginator.append()
        }
    }

    private fun refresh() {
        subCreator.unsubAll()
        val nprofile = createNprofile(hex = profile.value.value.inner.pubkey)
        profile.value = profileProvider.getProfileFlow(nprofile = nprofile)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                FullProfileUI(inner = profile.value.value.inner)
            )
        paginator.refresh()
    }
}
