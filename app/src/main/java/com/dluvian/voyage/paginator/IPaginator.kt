package com.dluvian.voyage.paginator

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import rust.nostr.sdk.Event

interface IPaginator {
    val isRefreshing: State<Boolean>
    val isSwitchingPage: State<Boolean>
    val isNotFirstPage: State<Boolean>
    val page: MutableState<List<Event>>
}
