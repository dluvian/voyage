package com.dluvian.voyage

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.room.Room
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.RelayedValidatedEvent
import com.dluvian.voyage.data.account.AccountManager
import com.dluvian.voyage.data.account.AccountSwitcher
import com.dluvian.voyage.data.account.ExternalSigner
import com.dluvian.voyage.data.account.MnemonicSigner
import com.dluvian.voyage.data.event.EventCacheClearer
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.event.EventProcessor
import com.dluvian.voyage.data.event.EventQueue
import com.dluvian.voyage.data.event.EventValidator
import com.dluvian.voyage.data.interactor.PostVoter
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.provider.AccountPubkeyProvider
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
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

    // Shared collections
    private val syncedEventQueueSet =
        Collections.synchronizedSet(mutableSetOf<RelayedValidatedEvent>())
    private val syncedFilterCache = Collections.synchronizedMap(mutableMapOf<SubId, List<Filter>>())
    private val syncedIdCache = Collections.synchronizedSet(mutableSetOf<EventIdHex>())
    private val syncedPostRelayCache = Collections
        .synchronizedSet(mutableSetOf<Pair<EventIdHex, RelayUrl>>())


    private val client = OkHttpClient()
    private val nostrClient = NostrClient(httpClient = client)
    private val mnemonicSigner = MnemonicSigner(context = context)
    val externalSigner = ExternalSigner()

    private val eventCacheClearer = EventCacheClearer(
        nostrClient = nostrClient,
        syncedEventQueue = syncedEventQueueSet,
        syncedIdCache = syncedIdCache,
        syncedPostRelayCache = syncedPostRelayCache
    )

    private val relayProvider = RelayProvider(nip65Dao = roomDb.nip65Dao())
    private val friendProvider = FriendProvider(friendDao = roomDb.friendDao())
    private val webOfTrustProvider = WebOfTrustProvider(webOfTrustDao = roomDb.webOfTrustDao())
    private val accountPubkeyProvider = AccountPubkeyProvider(accountDao = roomDb.accountDao())
    val topicProvider = TopicProvider(topicDao = roomDb.topicDao())

    val nostrSubscriber = NostrSubscriber(
        relayProvider = relayProvider,
        webOfTrustProvider = webOfTrustProvider,
        topicProvider = topicProvider,
        friendProvider = friendProvider,
        pubkeyProvider = accountPubkeyProvider,
        nostrClient = nostrClient,
        syncedFilterCache = syncedFilterCache,
    )
    private val accountSwitcher = AccountSwitcher(
        mnemonicSigner = mnemonicSigner,
        accountDao = roomDb.accountDao(),
        resetDao = roomDb.resetDao(),
        eventCacheClearer = eventCacheClearer,
        nostrSubscriber = nostrSubscriber
    )
    val accountManager = AccountManager(
        mnemonicSigner = mnemonicSigner,
        externalSigner = externalSigner,
        accountSwitcher = accountSwitcher,
        accountDao = roomDb.accountDao(),
    )
    private val eventValidator = EventValidator(
        syncedFilterCache = syncedFilterCache,
        syncedIdCache = syncedIdCache,
        syncedPostRelayCache = syncedPostRelayCache,
        pubkeyProvider = accountManager
    )
    private val eventProcessor = EventProcessor(
        postInsertDao = roomDb.postInsertDao(),
        voteUpsertDao = roomDb.voteUpsertDao(),
        friendUpsertDao = roomDb.friendUpsertDao(),
        webOfTrustUpsertDao = roomDb.webOfTrustUpsertDao(),
        topicUpsertDao = roomDb.topicUpsertDao(),
        nip65UpsertDao = roomDb.nip65UpsertDao(),
        pubkeyProvider = accountManager
    )
    private val eventQueue = EventQueue(
        syncedQueue = syncedEventQueueSet,
        eventValidator = eventValidator,
        eventProcessor = eventProcessor
    )
    private val eventMaker = EventMaker(accountManager = accountManager)
    val nostrService = NostrService(
        nostrClient = nostrClient,
        eventQueue = eventQueue,
        eventMaker = eventMaker,
        filterCache = syncedFilterCache
    )

    init {
        nostrService.initialize(initRelayUrls = relayProvider.getReadRelays())
    }

    val snackbarHostState = SnackbarHostState()

    val postVoter = PostVoter(
        nostrService = nostrService,
        relayProvider = relayProvider,
        snackbar = snackbarHostState,
        context = context,
        voteDao = roomDb.voteDao(),
        voteUpsertDao = roomDb.voteUpsertDao()
    )
    val feedProvider = FeedProvider(
        nostrSubscriber = nostrSubscriber,
        rootPostDao = roomDb.rootPostDao(),
        postVoter = postVoter
    )
}
