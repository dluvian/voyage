package com.dluvian.voyage.core.model

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.FEED_PAGE_SIZE
import com.dluvian.voyage.core.Fn
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.utils.launchIO
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.nostr.getCurrentSecs
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.ui.components.row.mainEvent.FeedCtx
import com.dluvian.voyage.ui.components.row.mainEvent.MainEventCtx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    override val hasMoreRecentItems = mutableStateOf(false)
    override val hasPage: MutableState<StateFlow<Boolean>> =
        mutableStateOf(MutableStateFlow(true))
    override val pageTimestamps: MutableState<List<Long>> = mutableStateOf(emptyList())
    override val filteredPage: MutableState<StateFlow<List<MainEventCtx>>> =
        mutableStateOf(MutableStateFlow(emptyList()))


    private lateinit var feedSetting: FeedSetting

    fun init(setting: FeedSetting) {
        if (isInitialized.value) return
        reinit(setting = setting)
    }

    fun reinit(setting: FeedSetting, showRefreshIndicator: Boolean = false) {
        isInitialized.value = true
        val isSame = pageTimestamps.value.isNotEmpty() && feedSetting == setting
        if (isSame) {
            Log.i(TAG, "Skip init. Settings are the same")
            return
        }
        if (showRefreshIndicator) isRefreshing.value = true

        hasPage.value = getHasPosts(setting = setting)
        hasMoreRecentItems.value = false
        feedSetting = setting

        scope.launch {
            setPage(until = getCurrentSecs())
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

        scope.launchIO {
            if (onSub != null) {
                onSub()
                delay(DELAY_1SEC)
            }
            setPage(until = getCurrentSecs(), forceSubscription = isFirstPage)
            delay(DELAY_1SEC)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }


    fun append() {
        if (isAppending.value || isRefreshing.value || pageTimestamps.value.isEmpty()) return

        subCreator.unsubAll()
        isAppending.value = true
        hasMoreRecentItems.value = true

        scope.launchIO {
            setPage(until = pageTimestamps.value.last() - 1)
            delay(SHORT_DEBOUNCE)
        }.invokeOnCompletion {
            isAppending.value = false
        }
    }

    private suspend fun setPage(
        until: Long,
        feedSetting: FeedSetting = this.feedSetting,
        forceSubscription: Boolean = false
    ) {
        val flow = feedProvider.getFeedFlow(
            until = until,
            size = FEED_PAGE_SIZE,
            setting = feedSetting,
            forceSubscription = forceSubscription,
        )

        filteredPage.value = flow
            .onEach { pageTimestamps.value = it.map { post -> post.createdAt } }
            .map { list -> list.map { FeedCtx(mainEvent = it) } }
            // No duplicate cross-posts
            .map { postCtx -> postCtx.distinctBy { it.mainEvent.getRelevantId() } }
            // Reported bug that LazyCol id has duplicates
            // TODO: Will be fixed once we move to in-memory view instead of room-view
            .map { postCtx -> postCtx.distinctBy { it.mainEvent.id } }
            .stateIn(scope, SharingStarted.WhileSubscribed(), getStaticFeed(until = until))
    }

    private suspend fun getStaticFeed(until: Long): List<MainEventCtx> {
        return feedProvider.getStaticFeed(
            until = until,
            size = FEED_PAGE_SIZE.div(6),
            setting = feedSetting
        ).map { FeedCtx(mainEvent = it) }
    }

    private fun getHasPosts(setting: FeedSetting) = feedProvider
        .settingHasPostsFlow(setting = setting)
        .stateIn(scope, SharingStarted.Eagerly, true)
}
