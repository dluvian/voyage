package com.dluvian.voyage

import android.content.Context
import androidx.room.Room
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.data.NostrService
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.event.EventProcessor
import com.dluvian.voyage.data.event.EventQueue
import com.dluvian.voyage.data.event.EventQueueQualityGate
import com.dluvian.voyage.data.keys.AccountKeyManager
import com.dluvian.voyage.data.keys.SingleUseKeyManager
import com.dluvian.voyage.data.room.AppDatabase
import okhttp3.OkHttpClient
import rust.nostr.protocol.Filter
import java.util.Collections

class AppContainer(context: Context) {
    val roomDb: AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "voyage_database",
    ).build()
    private val client = OkHttpClient()
    private val nostrClient = NostrClient(httpClient = client)
    private val filterCache = Collections.synchronizedMap(mutableMapOf<SubId, List<Filter>>())
    private val eventQueueQualityGate = EventQueueQualityGate(filterCache = filterCache)
    private val eventProcessor = EventProcessor()
    private val eventQueue = EventQueue(
        qualityGate = eventQueueQualityGate,
        eventProcessor = eventProcessor
    )
    private val singleUseKeyManager = SingleUseKeyManager(context)
    private val accountKeyManager = AccountKeyManager(context)
    private val eventMaker = EventMaker(
        singleUseKeyManager = singleUseKeyManager,
        accountKeyManager = accountKeyManager
    )
    val nostrService = NostrService(
        nostrClient = nostrClient,
        eventQueue = eventQueue,
        eventMaker = eventMaker,
        filterCache = filterCache
    )

    init {
        // TODO: Use nip65
        nostrService.initialize(
            initRelayUrls = listOf(
                "wss://nos.lol",
                "wss://nostr.fmt.wiz.biz",
                "wss://nostr.oxtr.dev",
                "wss://nostr.sethforprivacy.com",
                "wss://relay.nostr.wirednet.jp",
            )
        )
    }
}
