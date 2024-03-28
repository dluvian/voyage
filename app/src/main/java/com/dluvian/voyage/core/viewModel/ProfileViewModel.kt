package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.ProfileViewAction
import com.dluvian.voyage.core.ProfileViewAppend
import com.dluvian.voyage.core.ProfileViewFollowProfile
import com.dluvian.voyage.core.ProfileViewRefresh
import com.dluvian.voyage.core.ProfileViewUnfollowProfile
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.data.interactor.ProfileFollower
import com.dluvian.voyage.data.model.FullProfile
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.ProfileProvider
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    feedProvider: FeedProvider,
    private val nostrSubscriber: NostrSubscriber,
    private val profileFollower: ProfileFollower,
    private val profileProvider: ProfileProvider,
) : ViewModel() {
    val paginator = Paginator(feedProvider = feedProvider, scope = viewModelScope)
    val profile: MutableState<StateFlow<FullProfile>> =
        mutableStateOf(MutableStateFlow(FullProfile()))

    fun openProfile(profileNavView: ProfileNavView) {
        val pubkeyHex = profileNavView.nprofile.publicKey().toHex()
        if (profile.value.value.inner.pubkey == pubkeyHex) return

        paginator.init(setting = ProfileFeedSetting(pubkey = pubkeyHex))
        viewModelScope.launchIO {
            nostrSubscriber.subProfile(nprofile = profileNavView.nprofile)
        }

        profile.value = profileProvider.getProfileFlow(pubkey = pubkeyHex)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                FullProfile(inner = AdvancedProfileView(pubkey = pubkeyHex))
            )
    }

    fun handle(action: ProfileViewAction) {
        when (action) {
            is ProfileViewRefresh -> paginator.refresh()
            is ProfileViewAppend -> paginator.append()
            is ProfileViewFollowProfile -> profileFollower.follow(pubkey = action.pubkey)
            is ProfileViewUnfollowProfile -> profileFollower.unfollow(pubkey = action.pubkey)
        }
    }
}
