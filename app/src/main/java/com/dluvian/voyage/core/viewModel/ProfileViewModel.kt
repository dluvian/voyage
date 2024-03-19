package com.dluvian.voyage.core.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dluvian.voyage.core.ProfileViewAction
import com.dluvian.voyage.core.ProfileViewAppend
import com.dluvian.voyage.core.ProfileViewFollowProfile
import com.dluvian.voyage.core.ProfileViewRefresh
import com.dluvian.voyage.core.ProfileViewUnfollowProfile
import com.dluvian.voyage.core.model.Paginator
import com.dluvian.voyage.core.navigator.ProfileNavView
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.provider.FeedProvider
import kotlin.random.Random

class ProfileViewModel(feedProvider: FeedProvider) : ViewModel() {
    val name = mutableStateOf("")
    val pubkey = mutableStateOf("")
    val isFollowed = mutableStateOf(false)

    val paginator = Paginator(feedProvider = feedProvider, scope = viewModelScope)

    fun openProfile(profileNavView: ProfileNavView) {
        // TODO: sub profile
        val pubkeyHex = profileNavView.nip19Profile.publicKey().toHex()
        paginator.init(setting = ProfileFeedSetting(pubkey = pubkeyHex))

        name.value = "name:$pubkeyHex"
        pubkey.value = pubkeyHex
        isFollowed.value = Random.nextBoolean()
    }


    fun handle(action: ProfileViewAction) {
        when (action) {
            is ProfileViewRefresh -> paginator.refresh()
            is ProfileViewAppend -> paginator.append()
            is ProfileViewFollowProfile -> {
                // TODO: Profile follower
                isFollowed.value = true
            }

            is ProfileViewUnfollowProfile -> {
                // TODO: Profile follower
                isFollowed.value = false
            }
        }
    }
}
