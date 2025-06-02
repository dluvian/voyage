package com.dluvian.voyage.paginator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.PAGE_SIZE
import com.dluvian.voyage.filterSetting.FeedSetting
import com.dluvian.voyage.provider.FeedProvider
import rust.nostr.sdk.Event
import rust.nostr.sdk.Timestamp


class Paginator(private val feedProvider: FeedProvider) : IPaginator {
    override val isRefreshing = mutableStateOf(false)
    override val isSwitchingPage = mutableStateOf(false)
    override val isNotFirstPage = mutableStateOf(false)
    override val page = mutableStateOf(emptyList<Event>())

    private lateinit var feedSetting: FeedSetting

    override suspend fun load(setting: FeedSetting) {
        feedSetting = setting

        refresh()
    }

    override suspend fun refresh() {
        if (isRefreshing.value) return

        isRefreshing.value = true

        page.value = feedProvider.buildFeed(until = Timestamp.now(), setting = feedSetting)

        isRefreshing.value = false
    }

    override suspend fun dbRefreshInPlace() {
        // TODO: Wait for comparable Timestamps
        val until = page.value.maxByOrNull { it.createdAt().asSecs() }?.createdAt()
        page.value = feedProvider.buildFeed(
            until = until ?: Timestamp.now(),
            setting = feedSetting,
            dbOnly = true
        )
    }

    override suspend fun nextPage() {
        if (isSwitchingPage.value || isRefreshing.value || page.value.size < PAGE_SIZE) return

        isSwitchingPage.value = true
        isNotFirstPage.value = true

        val oldest = page.value
            .minBy { it.createdAt().asSecs() }
            .createdAt() // TODO: Wait for comparable Timestamps
        val untilSecs = oldest.asSecs() - 1u // TODO: Wait for arithmetics
        val until = Timestamp.fromSecs(untilSecs)
        page.value = feedProvider.buildFeed(
            until = until,
            setting = feedSetting,
            dbOnly = false
        )

        isSwitchingPage.value = false
    }
}
