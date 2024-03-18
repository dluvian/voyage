package com.dluvian.voyage.core.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.nostr_kt.getCurrentSecs
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.FEED_OFFSET
import com.dluvian.voyage.core.FEED_PAGE_SIZE
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.data.provider.FeedProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class Paginator(private val feedProvider: FeedProvider, private val scope: CoroutineScope) :
    IPaginator {
    override val isRefreshing = mutableStateOf(false)
    override val isAppending = mutableStateOf(false)
    override val page: MutableState<StateFlow<List<RootPost>>> =
        mutableStateOf(MutableStateFlow(emptyList()))

    fun init() {
        page.value = feedProvider.getFeedFlow(until = getCurrentSecs(), size = FEED_PAGE_SIZE)
            .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())
    }

    fun refresh(onSub: Fn = {}) {
        if (isRefreshing.value) return

        isRefreshing.value = true

        scope.launch(Dispatchers.IO) {
            onSub()
            delay(DELAY_1SEC)
            val initValue = page.value.value.take(FEED_PAGE_SIZE)
            page.value = getFlow(until = getCurrentSecs())
                .stateIn(scope, SharingStarted.WhileSubscribed(), initValue)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }


    fun append() {
        if (isAppending.value || isRefreshing.value || page.value.value.isEmpty()) return

        isAppending.value = true

        scope.launch(Dispatchers.IO) {
            val newUntil = page.value.value.takeLast(FEED_OFFSET).first().createdAt
            val initValue = page.value.value.takeLast(FEED_OFFSET)
            page.value = getFlow(until = newUntil)
                .stateIn(scope, SharingStarted.WhileSubscribed(), initValue)
        }.invokeOnCompletion {
            isAppending.value = false
        }
    }

    private fun getFlow(until: Long): Flow<List<RootPost>> {
        return feedProvider.getFeedFlow(until = until, size = FEED_PAGE_SIZE)
    }
}
