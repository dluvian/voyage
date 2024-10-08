package com.dluvian.voyage.core.model

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.FEED_OFFSET
import com.dluvian.voyage.core.FEED_PAGE_SIZE
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.utils.containsNoneIgnoreCase
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.model.BookmarksFeedSetting
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.ListFeedSetting
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.model.ReplyFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.nostr.getCurrentSecs
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.MuteProvider
import com.dluvian.voyage.ui.components.row.mainEvent.FeedCtx
import com.dluvian.voyage.ui.components.row.mainEvent.MainEventCtx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "Paginator"

class Paginator(
    private val feedProvider: FeedProvider,
    private val muteProvider: MuteProvider,
    private val subCreator: SubscriptionCreator,
    private val scope: CoroutineScope,
) : IPaginator {
    override val isInitialized = mutableStateOf(false)
    override val isRefreshing = mutableStateOf(false)
    override val isAppending = mutableStateOf(false)
    override val hasMoreRecentItems = mutableStateOf(false)
    override val hasPage: MutableState<StateFlow<Boolean>> =
        mutableStateOf(MutableStateFlow(true))
    override val page: MutableState<StateFlow<List<MainEventCtx>>> =
        mutableStateOf(MutableStateFlow(emptyList()))
    override val filteredPage: MutableState<StateFlow<List<MainEventCtx>>> =
        mutableStateOf(MutableStateFlow(emptyList()))


    private lateinit var feedSetting: FeedSetting

    fun init(setting: FeedSetting) {
        if (isInitialized.value) return
        reinit(setting = setting)
    }

    fun reinit(setting: FeedSetting, showRefreshIndicator: Boolean = false) {
        isInitialized.value = true
        val isSame = page.value.value.isNotEmpty() && feedSetting == setting
        if (isSame) {
            Log.i(TAG, "Skip init. Settings are the same")
            return
        }
        if (showRefreshIndicator) isRefreshing.value = true

        hasPage.value = getHasPosts(setting = setting)
        hasMoreRecentItems.value = false
        feedSetting = setting

        val now = getCurrentSecs()

        scope.launch {
            setPage(
                until = now,
                subUntil = now,
            )
            delay(DELAY_1SEC)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }

    fun refresh(onSub: Fn? = null) {
        if (isRefreshing.value) return

        val isFirstPage = !hasMoreRecentItems.value

        isRefreshing.value = true
        hasMoreRecentItems.value = false
        hasPage.value = getHasPosts(setting = feedSetting)

        val now = getCurrentSecs()

        scope.launchIO {
            if (onSub != null) {
                onSub()
                delay(DELAY_1SEC)
            }
            setPage(
                until = now,
                subUntil = now,
                forceSubscription = isFirstPage
            )
            delay(DELAY_1SEC)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }


    fun append() {
        if (isAppending.value || isRefreshing.value || page.value.value.isEmpty()) return

        subCreator.unsubAll()
        isAppending.value = true
        hasMoreRecentItems.value = true

        scope.launchIO {
            val newUntil = page.value.value.takeLast(FEED_OFFSET).first().mainEvent.createdAt
            val subUntil = page.value.value.last().mainEvent.createdAt - 1
            setPage(until = newUntil, subUntil = subUntil)
            delay(DELAY_1SEC)
        }.invokeOnCompletion {
            isAppending.value = false
        }
    }

    private suspend fun setPage(
        until: Long,
        subUntil: Long,
        feedSetting: FeedSetting = this.feedSetting,
        forceSubscription: Boolean = false
    ) {
        val staticFeed = getStaticFeed(until = until)
        val flow = feedProvider.getFeedFlow(
            until = until,
            subUntil = subUntil,
            size = FEED_PAGE_SIZE,
            setting = feedSetting,
            forceSubscription = forceSubscription,
        ).map { list -> list.map { FeedCtx(mainEvent = it) } }
        val mutedWords = muteProvider.getMutedWords()

        page.value = flow.stateIn(scope, SharingStarted.WhileSubscribed(), staticFeed)
        filteredPage.value = flow
            // No duplicate cross-posts
            .map { postCtx -> postCtx.distinctBy { it.mainEvent.getRelevantId() } }
            .map { postCtxs ->
                when (feedSetting) {
                    // No muted words
                    is HomeFeedSetting, is TopicFeedSetting,
                    is InboxFeedSetting, is ListFeedSetting -> {
                        postCtxs.filter { postCtx ->
                            postCtx.mainEvent.trustType == Oneself ||
                            postCtx.mainEvent.content.text.containsNoneIgnoreCase(strs = mutedWords)
                        }
                    }
                    // Muted words allowed
                    BookmarksFeedSetting, is ReplyFeedSetting, is ProfileFeedSetting -> postCtxs
                }
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(), staticFeed)
    }

    private suspend fun getStaticFeed(until: Long): List<MainEventCtx> {
        return feedProvider.getStaticFeed(
            until = until,
            size = FEED_PAGE_SIZE.div(2),
            setting = feedSetting
        ).map { FeedCtx(mainEvent = it) }
    }

    private fun getHasPosts(setting: FeedSetting) = feedProvider
        .settingHasPostsFlow(setting = setting)
        .stateIn(scope, SharingStarted.Eagerly, true)
}
