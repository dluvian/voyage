package com.dluvian.voyage.paginator

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.NostrService
import com.dluvian.voyage.PAGE_SIZE
import com.dluvian.voyage.filterSetting.FeedSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.sdk.Event
import rust.nostr.sdk.Timestamp

private const val TAG = "Paginator"

class Paginator(
    private val service: NostrService,
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
            setPage(until = Timestamp.now())
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
                .createdAt()// TODO: Ask to make Timestamp comparable
            val untilSecs = oldest.asSecs() - 1u // TODO: Ask to add arithmetic to Timestamp
            val until = Timestamp.fromSecs(untilSecs)
            setPage(until)
        }.invokeOnCompletion {
            isSwitchingPage.value = false
        }
    }

    private suspend fun setPage(until: Timestamp) {
        TODO("Get pagesize until + all posts of oldest timestamp")
    }
}
