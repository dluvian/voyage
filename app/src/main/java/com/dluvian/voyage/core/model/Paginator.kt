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
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.ProfileFeedSetting
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


class Paginator(
    private val feedProvider: FeedProvider,
    private val subCreator: SubscriptionCreator,
    private val scope: CoroutineScope
) : IPaginator {
    private val tag = "Paginator"
    override val isRefreshing = mutableStateOf(false)
    override val isAppending = mutableStateOf(false)
    override val page: MutableState<StateFlow<List<RootPostUI>>> =
        mutableStateOf(MutableStateFlow(emptyList()))

    private lateinit var feedSetting: FeedSetting

    fun init(setting: FeedSetting) {
        val isSame = when (setting) {
            is HomeFeedSetting -> page.value.value.isNotEmpty()
            is TopicFeedSetting -> page.value.value.isNotEmpty() && feedSetting == setting
            is ProfileFeedSetting -> page.value.value.isNotEmpty() && feedSetting == setting
        }
        if (isSame) {
            Log.i(tag, "Skip init. Settings are the same")
            return
        }
        val now = getCurrentSecs()
        feedSetting = setting

        scope.launch {
            val init = feedProvider.getStaticFeed(
                until = now,
                size = FEED_PAGE_SIZE,
                setting = setting
            )

            page.value = getFlow(until = now).stateIn(scope, SharingStarted.Eagerly, init)
        }
    }

    fun refresh(onSub: Fn = {}) {
        if (isRefreshing.value) return

        isRefreshing.value = true

        scope.launchIO {
            onSub()
            delay(DELAY_1SEC)
            val initValue = page.value.value.take(FEED_PAGE_SIZE)
            page.value =
                getFlow(until = getCurrentSecs()).stateIn(scope, SharingStarted.Eagerly, initValue)
        }.invokeOnCompletion {
            isRefreshing.value = false
        }
    }


    fun append() {
        if (isAppending.value || isRefreshing.value || page.value.value.isEmpty()) return

        subCreator.unsubAll()
        isAppending.value = true

        scope.launchIO {
            val newUntil = page.value.value.takeLast(FEED_OFFSET).first().createdAt
            val initValue = page.value.value.takeLast(FEED_OFFSET)
            page.value = getFlow(until = newUntil).stateIn(scope, SharingStarted.Eagerly, initValue)
        }.invokeOnCompletion {
            isAppending.value = false
        }
    }

    private suspend fun getFlow(until: Long): Flow<List<RootPostUI>> {
        return feedProvider.getFeedFlow(
            until = until,
            size = FEED_PAGE_SIZE,
            setting = feedSetting
        )
    }
}
