package com.dluvian.voyage.paginator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.PAGE_SIZE
import com.dluvian.voyage.filterSetting.FeedSetting
import com.dluvian.voyage.provider.FeedProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.sdk.Event
import rust.nostr.sdk.Timestamp


class Paginator(
    private val feedProvider: FeedProvider,
    private val scope: CoroutineScope,
) : IPaginator {
    override val isRefreshing = mutableStateOf(false)
    override val isSwitchingPage = mutableStateOf(false)
    override val isNotFirstPage = mutableStateOf(false)
    override val page = mutableStateOf(emptyList<Event>())

    private lateinit var feedSetting: FeedSetting

    fun load(setting: FeedSetting) {
        feedSetting = setting

        refresh()
    }


    fun refresh() {
        if (isRefreshing.value) return

        isRefreshing.value = true

        scope.launch(Dispatchers.IO) {
            page.value = feedProvider.buildFeed(until = Timestamp.now(), setting = feedSetting)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }


    fun append() {
        if (isSwitchingPage.value || isRefreshing.value || page.value.size < PAGE_SIZE) return

        isSwitchingPage.value = true
        isNotFirstPage.value = true

        scope.launch(Dispatchers.IO) {
            val oldest = page.value.minBy { it.createdAt().asSecs() }
                .createdAt()// TODO: Wait for comparable Timestamps
            val untilSecs = oldest.asSecs() - 1u // TODO: Wait for arithmetics
            val until = Timestamp.fromSecs(untilSecs)
            page.value =
                feedProvider.buildFeed(until = until, setting = feedSetting)
        }.invokeOnCompletion {
            isSwitchingPage.value = false
        }
    }
}
