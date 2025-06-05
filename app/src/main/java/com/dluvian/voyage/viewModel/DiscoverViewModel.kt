package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.TopicFollowState
import com.dluvian.voyage.model.DiscoverViewCmd
import com.dluvian.voyage.model.DiscoverViewOpen
import com.dluvian.voyage.model.DiscoverViewRefresh
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.provider.IEventUpdate
import com.dluvian.voyage.provider.TopicProvider
import rust.nostr.sdk.Event
import java.util.concurrent.atomic.AtomicBoolean


class DiscoverViewModel(
    private val topicProvider: TopicProvider,
    private val service: NostrService
) : ViewModel(), IEventUpdate {
    private val inViewAtLeastOnce = AtomicBoolean(false)
    private val maxCount = 75

    val isRefreshing = mutableStateOf(false)
    val topics = mutableStateOf(emptyList<TopicFollowState>())
    val profiles = mutableStateOf(emptyList<TrustProfile>())

    fun handle(cmd: DiscoverViewCmd) {
        when (cmd) {
            DiscoverViewOpen -> TODO()
            DiscoverViewRefresh -> TODO()
        }
    }

    override suspend fun update(event: Event) {
        TODO("Not yet implemented")
    }
}
