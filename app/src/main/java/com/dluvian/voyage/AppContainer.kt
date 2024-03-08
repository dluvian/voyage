package com.dluvian.voyage

import android.content.Context
import androidx.room.Room
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.data.FeedProvider
import com.dluvian.voyage.data.NostrService
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.event.EventProcessor
import com.dluvian.voyage.data.event.EventQueue
import com.dluvian.voyage.data.event.EventValidator
import com.dluvian.voyage.data.keys.AccountKeyManager
import com.dluvian.voyage.data.keys.MnemonicManager
import com.dluvian.voyage.data.room.AppDatabase
import okhttp3.OkHttpClient
import rust.nostr.protocol.Filter
import java.util.Collections

class AppContainer(context: Context) {
    private val roomDb: AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "voyage_database",
    ).build()
    private val mnemonicManager = MnemonicManager(context = context)
    private val accountKeyManager = AccountKeyManager(
        context = context,
        mnemonicManager = mnemonicManager,
        accountDao = roomDb.accountDao(),
    )
    private val client = OkHttpClient()
    private val nostrClient = NostrClient(httpClient = client)
    private val filterCache = Collections.synchronizedMap(mutableMapOf<SubId, List<Filter>>())
    private val eventValidator =
        EventValidator(filterCache = filterCache, pubkeyProvider = accountKeyManager)
    private val eventProcessor = EventProcessor(
        postInsertDao = roomDb.postInsertDao(),
        voteUpsertDao = roomDb.voteUpsertDao(),
        friendUpsertDao = roomDb.friendUpsertDao(),
        webOfTrustUpsertDao = roomDb.webOfTrustUpsertDao(),
        topicUpsertDao = roomDb.topicUpsertDao(),
        pubkeyProvider = accountKeyManager
    )
    private val eventQueue = EventQueue(
        eventValidator = eventValidator,
        eventProcessor = eventProcessor
    )
    private val eventMaker = EventMaker(
        singleUseKeyManager = mnemonicManager,
        accountKeyManager = accountKeyManager
    )
    val nostrService = NostrService(
        nostrClient = nostrClient,
        eventQueue = eventQueue,
        eventMaker = eventMaker,
        filterCache = filterCache
    )
    val feedProvider = FeedProvider()

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
