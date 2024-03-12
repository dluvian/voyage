package com.dluvian.voyage

import android.content.Context
import androidx.room.Room
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.event.EventProcessor
import com.dluvian.voyage.data.event.EventQueue
import com.dluvian.voyage.data.event.EventValidator
import com.dluvian.voyage.data.interactor.PostVoter
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import com.dluvian.voyage.data.room.AppDatabase
import com.dluvian.voyage.data.signer.AccountManager
import com.dluvian.voyage.data.signer.ExternalSigner
import com.dluvian.voyage.data.signer.MnemonicSigner
import okhttp3.OkHttpClient
import rust.nostr.protocol.Filter
import java.util.Collections

class AppContainer(context: Context) {
    private val roomDb: AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "voyage_database",
    ).build()
    private val mnemonicSigner = MnemonicSigner(context = context)
    private val externalSigner = ExternalSigner()
    val accountManager = AccountManager(
        mnemonicSigner = mnemonicSigner,
        externalSigner = externalSigner,
        accountDao = roomDb.accountDao(),
    )
    private val client = OkHttpClient()
    private val nostrClient = NostrClient(httpClient = client)
    private val filterCache = Collections.synchronizedMap(mutableMapOf<SubId, List<Filter>>())
    private val eventValidator = EventValidator(
        filterCache = filterCache,
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
        eventValidator = eventValidator,
        eventProcessor = eventProcessor
    )
    private val eventMaker = EventMaker(accountManager = accountManager)
    val nostrService = NostrService(
        nostrClient = nostrClient,
        eventQueue = eventQueue,
        eventMaker = eventMaker,
        filterCache = filterCache
    )
    private val relayProvider = RelayProvider(nip65Dao = roomDb.nip65Dao())

    init {
        nostrService.initialize(initRelayUrls = relayProvider.getReadRelays())
    }

    private val topicProvider = TopicProvider(topicDao = roomDb.topicDao())
    private val friendProvider = FriendProvider(friendDao = roomDb.friendDao())
    private val webOfTrustProvider = WebOfTrustProvider(webOfTrustDao = roomDb.webOfTrustDao())
    val postVoter = PostVoter(nostrService, relayProvider, roomDb.voteDao(), roomDb.voteUpsertDao())
    private val nostrSubscriber = NostrSubscriber(
        nostrService = nostrService,
        relayProvider = relayProvider,
        webOfTrustProvider = webOfTrustProvider,
        topicProvider = topicProvider,
        friendProvider = friendProvider,
        pubkeyProvider = accountManager,
    )
    val feedProvider = FeedProvider(
        nostrSubscriber = nostrSubscriber,
        rootPostDao = roomDb.rootPostDao(),
        postVoter = postVoter
    )
}
