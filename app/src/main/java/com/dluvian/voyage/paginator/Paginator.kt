package com.dluvian.voyage.paginator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.PAGE_SIZE
import com.dluvian.voyage.filterSetting.BookmarkFeedSetting
import com.dluvian.voyage.filterSetting.FeedSetting
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.filterSetting.InboxFeedSetting
import com.dluvian.voyage.filterSetting.ListFeedSetting
import com.dluvian.voyage.filterSetting.ProfileFeedSetting
import com.dluvian.voyage.filterSetting.TopicFeedSetting
import com.dluvian.voyage.model.UIEvent
import com.dluvian.voyage.provider.FeedProvider
import rust.nostr.sdk.Event
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Timestamp


class Paginator(
    private val feedProvider: FeedProvider,
) : IPaginator {
    override val isRefreshing = mutableStateOf(false)
    override val isSwitchingPage = mutableStateOf(false)
    override val isNotFirstPage = mutableStateOf(false)
    override val showNextPageBtn = mutableStateOf(false)
    override val page = mutableStateOf(emptyList<UIEvent>())

    private lateinit var feedSetting: FeedSetting

    override fun initSetting(setting: FeedSetting) {
        feedSetting = setting
    }

    override suspend fun refresh() {
        if (isRefreshing.value) return

        isRefreshing.value = true

        page.value = feedProvider.buildFeed(until = Timestamp.now(), setting = feedSetting)

        isRefreshing.value = false
        showNextPageBtn.value = showNextPageBtn()
    }

    override suspend fun dbRefreshInPlace() {
        val until = page.value.maxByOrNull { it.event.createdAt().asSecs() }?.event?.createdAt()
        page.value = feedProvider.buildFeed(
            until = until ?: Timestamp.now(),
            setting = feedSetting,
            dbOnly = true
        )
        showNextPageBtn.value = showNextPageBtn()
    }

    override suspend fun nextPage() {
        if (isSwitchingPage.value || isRefreshing.value || page.value.size < PAGE_SIZE) return

        isSwitchingPage.value = true
        isNotFirstPage.value = true

        val oldest = page.value
            .minBy { it.event.createdAt().asSecs() }
            .event
            .createdAt()
        val untilSecs = oldest.asSecs() - 1u
        val until = Timestamp.fromSecs(untilSecs)
        page.value = feedProvider.buildFeed(
            until = until,
            setting = feedSetting,
            dbOnly = false
        )

        isSwitchingPage.value = false
        showNextPageBtn.value = showNextPageBtn()
    }

    private val relevantKinds = listOf(
        KindStandard.CONTACT_LIST, KindStandard.METADATA,
        KindStandard.INTERESTS, KindStandard.BOOKMARKS
    ).map { Kind.fromStd(it) }

    override suspend fun update(event: Event) {
        val kinds = when (val setting = feedSetting) {
            is BookmarkFeedSetting -> null
            is HomeFeedSetting -> setting.kinds
            is InboxFeedSetting -> setting.kinds
            is ListFeedSetting -> setting.kinds
            is ProfileFeedSetting -> setting.kinds
            is TopicFeedSetting -> setting.kinds
        }

        if (kinds == null) {
            dbRefreshInPlace()
            return
        }

        if (kinds.contains(event.kind()) || relevantKinds.contains(event.kind())) {
            dbRefreshInPlace()
        }
    }

    private fun showNextPageBtn() = page.value.size >= feedSetting.pageSize.toInt()
}
