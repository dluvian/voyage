package com.dluvian.voyage.core.model

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.nostr_kt.getCurrentSecs
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.FEED_OFFSET
import com.dluvian.voyage.core.FEED_PAGE_SIZE
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ListFeedSetting
import com.dluvian.voyage.data.model.ProfileRootFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.provider.FeedProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "Paginator"

class Paginator(
    private val feedProvider: FeedProvider,
    private val subCreator: SubscriptionCreator,
    private val scope: CoroutineScope,
) : IPaginator {
    override val isInitialized = mutableStateOf(false)
    override val isRefreshing = mutableStateOf(false)
    override val isAppending = mutableStateOf(false)
    override val hasMoreRecentPosts = mutableStateOf(false)
    override val hasPosts: MutableState<StateFlow<Boolean>> =
        mutableStateOf(MutableStateFlow(true))
    override val page: MutableState<StateFlow<List<ParentUI>>> =
        mutableStateOf(MutableStateFlow(emptyList()))

    private lateinit var feedSetting: FeedSetting

    fun init(setting: FeedSetting) {
        if (isInitialized.value) return
        reinit(setting = setting)
    }

    fun reinit(setting: FeedSetting) {
        isInitialized.value = true
        val isSame = when (setting) {
            HomeFeedSetting, InboxFeedSetting, BookmarksFeedSetting -> page.value.value.isNotEmpty()
            is TopicFeedSetting,
            is ProfileRootFeedSetting,
            is ReplyFeedSetting,
            is ListFeedSetting -> page.value.value.isNotEmpty() && feedSetting == setting
        }
        if (isSame) {
            Log.i(TAG, "Skip init. Settings are the same")
            return
        }

        hasPosts.value = getHasPosts(setting = setting)
        hasMoreRecentPosts.value = false
        feedSetting = setting

        val now = getCurrentSecs()

        scope.launch {
            page.value =
                getFlow(until = now, subUntil = now, subscribe = feedSetting !is ReplyFeedSetting)
                    .stateIn(scope, SharingStarted.WhileSubscribed(), getStaticFeed(until = now))
        }
    }

    fun refresh(onSub: Fn = {}) {
        if (isRefreshing.value) return

        isRefreshing.value = true
        hasMoreRecentPosts.value = false
        hasPosts.value = getHasPosts(setting = feedSetting)

        val now = getCurrentSecs()

        scope.launchIO {
            onSub()
            delay(DELAY_1SEC)
            page.value =
                getFlow(
                    until = now,
                    subUntil = now,
                    subscribe = feedSetting !is ReplyFeedSetting,
                    forceSubscription = true
                )
                    .stateIn(scope, SharingStarted.WhileSubscribed(), getStaticFeed(until = now))
            delay(DELAY_1SEC)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }


    fun append() {
        if (isAppending.value || isRefreshing.value || page.value.value.isEmpty()) return

        subCreator.unsubAll()
        isAppending.value = true
        hasMoreRecentPosts.value = true

        scope.launchIO {
            val newUntil = page.value.value.takeLast(FEED_OFFSET).first().createdAt
            val subUntil = page.value.value.last().createdAt - 1
            page.value = getFlow(until = newUntil, subUntil = subUntil)
                .stateIn(scope, SharingStarted.WhileSubscribed(), getStaticFeed(until = newUntil))
            delay(DELAY_1SEC)
        }.invokeOnCompletion {
            isAppending.value = false
        }
    }

    private suspend fun getFlow(
        until: Long,
        subUntil: Long,
        subscribe: Boolean = true,
        forceSubscription: Boolean = false
    ): Flow<List<ParentUI>> {
        return feedProvider.getFeedFlow(
            until = until,
            subUntil = subUntil,
            size = FEED_PAGE_SIZE,
            setting = feedSetting,
            forceSubscription = forceSubscription,
            subscribe = subscribe
        )
    }

    private suspend fun getStaticFeed(until: Long): List<ParentUI> {
        return feedProvider.getStaticFeed(
            until = until,
            size = FEED_PAGE_SIZE.div(2),
            setting = feedSetting
        )
    }

    private fun getHasPosts(setting: FeedSetting) = feedProvider
        .settingHasPostsFlow(setting = setting)
        .stateIn(scope, SharingStarted.Eagerly, true)
}
