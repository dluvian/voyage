package com.dluvian.voyage

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.room.Room
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.data.account.AccountManager
import com.dluvian.voyage.data.account.AccountSwitcher
import com.dluvian.voyage.data.account.ExternalSigner
import com.dluvian.voyage.data.account.MnemonicSigner
import com.dluvian.voyage.data.event.EventCacheClearer
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.event.EventProcessor
import com.dluvian.voyage.data.event.EventQueue
import com.dluvian.voyage.data.event.EventSweeper
import com.dluvian.voyage.data.event.EventValidator
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.event.ValidatedEvent
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.interactor.PostSender
import com.dluvian.voyage.data.interactor.PostVoter
import com.dluvian.voyage.data.interactor.ProfileFollower
import com.dluvian.voyage.data.interactor.ThreadCollapser
import com.dluvian.voyage.data.interactor.TopicFollower
import com.dluvian.voyage.data.model.FilterWrapper
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.preferences.DatabasePreferences
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.provider.DatabaseStatProvider
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.NameProvider
import com.dluvian.voyage.data.provider.ProfileProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.SuggestionProvider
import com.dluvian.voyage.data.provider.ThreadProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import com.dluvian.voyage.data.room.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import java.util.Collections

class AppContainer(context: Context) {
    val roomDb: AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "voyage_database",
    ).build()

    // Shared collections
    private val syncedEventQueueSet = Collections.synchronizedSet(mutableSetOf<ValidatedEvent>())
    private val syncedFilterCache = Collections
        .synchronizedMap(mutableMapOf<SubId, List<FilterWrapper>>())
    private val syncedIdCache = Collections.synchronizedSet(mutableSetOf<EventIdHex>())

    val snackbar = SnackbarHostState()
    private val client = OkHttpClient()
    private val nostrClient = NostrClient(httpClient = client)
    private val mnemonicSigner = MnemonicSigner(context = context)
    val externalSigner = ExternalSigner()

    private val eventCacheClearer = EventCacheClearer(
        syncedEventQueue = syncedEventQueueSet,
        syncedIdCache = syncedIdCache,
    )

    private val relayProvider = RelayProvider(
        nip65Dao = roomDb.nip65Dao(),
        eventRelayDao = roomDb.eventRelayDao()
    )
    private val friendProvider = FriendProvider(friendDao = roomDb.friendDao())
    private val webOfTrustProvider = WebOfTrustProvider(webOfTrustDao = roomDb.webOfTrustDao())

    private val forcedFollowStates = MutableStateFlow(emptyMap<Topic, Boolean>())

    val topicProvider =
        TopicProvider(topicDao = roomDb.topicDao(), forcedFollowStates = forcedFollowStates)

    private val accountManager = AccountManager(
        mnemonicSigner = mnemonicSigner,
        externalSigner = externalSigner,
        accountDao = roomDb.accountDao(),
    )
    val nostrSubscriber = NostrSubscriber(
        topicProvider = topicProvider,
        relayProvider = relayProvider,
        webOfTrustProvider = webOfTrustProvider,
        friendProvider = friendProvider,
        pubkeyProvider = accountManager,
        profileDao = roomDb.profileDao(),
        nostrClient = nostrClient,
        syncedFilterCache = syncedFilterCache,
    )
    val accountSwitcher = AccountSwitcher(
        accountManager = accountManager,
        accountDao = roomDb.accountDao(),
        resetDao = roomDb.resetDao(),
        eventCacheClearer = eventCacheClearer,
        nostrSubscriber = nostrSubscriber
    )

    private val metadataInMemory = MetadataInMemory()
    private val eventValidator = EventValidator(
        syncedFilterCache = syncedFilterCache,
        syncedIdCache = syncedIdCache,
        pubkeyProvider = accountManager
    )
    private val eventProcessor = EventProcessor(
        room = roomDb,
        metadataInMemory = metadataInMemory,
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

    val postVoter = PostVoter(
        nostrService = nostrService,
        relayProvider = relayProvider,
        snackbar = snackbar,
        context = context,
        voteDao = roomDb.voteDao(),
        voteUpsertDao = roomDb.voteUpsertDao()
    )

    val threadCollapser = ThreadCollapser()

    val topicFollower = TopicFollower(
        nostrService = nostrService,
        relayProvider = relayProvider,
        topicUpsertDao = roomDb.topicUpsertDao(),
        topicDao = roomDb.topicDao(),
        snackbar = snackbar,
        context = context,
        forcedFollowStates = forcedFollowStates
    )

    private val oldestUsedEvent = OldestUsedEvent()

    private val nameCache = Collections.synchronizedMap(mutableMapOf<PubkeyHex, String?>())

    private val nameProvider = NameProvider(
        profileDao = roomDb.profileDao(),
        nameCache = nameCache,
        nostrSubscriber = nostrSubscriber
    )

    private val annotatedStringProvider = AnnotatedStringProvider(
        nameProvider = nameProvider
    )

    val feedProvider = FeedProvider(
        nostrSubscriber = nostrSubscriber,
        rootPostDao = roomDb.rootPostDao(),
        forcedVotes = postVoter.forcedVotes,
        oldestUsedEvent = oldestUsedEvent,
        annotatedStringProvider = annotatedStringProvider,
        nameCache = nameCache
    )

    val threadProvider = ThreadProvider(
        nostrSubscriber = nostrSubscriber,
        rootPostDao = roomDb.rootPostDao(),
        replyDao = roomDb.replyDao(),
        forcedVotes = postVoter.forcedVotes,
        collapsedIds = threadCollapser.collapsedIds,
        annotatedStringProvider = annotatedStringProvider,
        nameCache = nameCache
    )

    val profileFollower = ProfileFollower(
        nostrService = nostrService,
        relayProvider = relayProvider,
        snackbar = snackbar,
        context = context,
        friendProvider = friendProvider,
        friendUpsertDao = roomDb.friendUpsertDao(),
    )

    val profileProvider = ProfileProvider(
        profileFollower = profileFollower, // TODO: Only inject flow
        pubkeyProvider = accountManager,
        metadataInMemory = metadataInMemory,
        profileDao = roomDb.profileDao(),
        friendProvider = friendProvider,
        nostrSubscriber = nostrSubscriber,
        annotatedStringProvider = annotatedStringProvider
    )

    val suggestionProvider = SuggestionProvider(
        topicProvider = topicProvider,
        profileProvider = profileProvider
    )

    val postSender = PostSender(
        nostrService = nostrService,
        relayProvider = relayProvider,
        postInsertDao = roomDb.postInsertDao()
    )

    val databasePreferences = DatabasePreferences(context = context)
    val databaseStatProvider = DatabaseStatProvider(countDao = roomDb.countDao())

    val eventSweeper = EventSweeper(
        databasePreferences = databasePreferences,
        eventCacheClearer = eventCacheClearer,
        deleteDao = roomDb.deleteDao(),
        oldestUsedEvent = oldestUsedEvent
    )
}
