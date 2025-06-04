package com.dluvian.voyage.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.SearchText
import com.dluvian.voyage.model.SearchViewCmd
import com.dluvian.voyage.model.SearchViewOpen
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.model.UpdateSearchText
import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.provider.IEventUpdate
import com.dluvian.voyage.provider.NameProvider
import rust.nostr.sdk.Event

class SearchViewModel(
    private val service: NostrService,
    private val nameProvider: NameProvider,
) : ViewModel(), IEventUpdate {
    val topics = mutableStateOf(emptyList<Topic>())
    val profiles = mutableStateOf(emptyList<TrustProfile>())

    fun handle(cmd: SearchViewCmd) {
        when (cmd) {
            SearchViewOpen -> TODO("Sub unknown profiles of interest")
            is SearchText -> TODO()
            is UpdateSearchText -> TODO()
        }
    }

    override suspend fun update(event: Event) {
        TODO("Update because of profiles")
    }
}
