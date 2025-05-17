package com.dluvian.voyage

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.room.Room
import com.anggrayudi.storage.SimpleStorageHelper
import com.dluvian.voyage.data.AccountSwitcher
import com.dluvian.voyage.data.KeyStore
import com.dluvian.voyage.data.event.EventCounter
import com.dluvian.voyage.data.event.EventDeletor
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.event.EventProcessor
import com.dluvian.voyage.data.event.EventQueue
import com.dluvian.voyage.data.event.EventRebroadcaster
import com.dluvian.voyage.data.event.EventSweeper
import com.dluvian.voyage.data.event.EventValidator
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.interactor.Bookmarker
import com.dluvian.voyage.data.interactor.ItemSetEditor
import com.dluvian.voyage.data.interactor.PostDetailInspector
import com.dluvian.voyage.data.interactor.PostSender
import com.dluvian.voyage.data.interactor.PostVoter
import com.dluvian.voyage.data.interactor.ProfileFollower
import com.dluvian.voyage.data.interactor.ThreadCollapser
import com.dluvian.voyage.data.interactor.TopicFollower
import com.dluvian.voyage.data.nostr.FilterCreator
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.nostr.SubBatcher
import com.dluvian.voyage.data.nostr.SubId
import com.dluvian.voyage.data.nostr.SubscriptionCreator
import com.dluvian.voyage.data.preferences.DatabasePreferences
import com.dluvian.voyage.data.preferences.EventPreferences
import com.dluvian.voyage.data.preferences.HomePreferences
import com.dluvian.voyage.data.preferences.InboxPreferences
import com.dluvian.voyage.data.preferences.RelayPreferences
import com.dluvian.voyage.data.provider.AnnotatedStringProvider
import com.dluvian.voyage.data.provider.DatabaseInteractor
import com.dluvian.voyage.data.provider.FeedProvider
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.NameProvider
import com.dluvian.voyage.data.provider.ProfileProvider
import com.dluvian.voyage.data.provider.PubkeyProvider
import com.dluvian.voyage.data.provider.RelayProfileProvider
import com.dluvian.voyage.data.provider.SearchProvider
import com.dluvian.voyage.data.provider.SuggestionProvider
import com.dluvian.voyage.data.provider.ThreadProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import com.dluvian.voyage.data.room.AppDatabase
import rust.nostr.sdk.ClientBuilder
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Options
import java.util.Collections

class AppContainer(val context: Context, storageHelper: SimpleStorageHelper) {
    val roomDb: AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "voyage_database",
    ).build()

    // Shared collections
    private val syncedFilterCache = Collections
        .synchronizedMap(mutableMapOf<SubId, List<Filter>>())
    private val syncedIdCache = Collections.synchronizedSet(mutableSetOf<EventId>())

    val snackbar = SnackbarHostState()

    val homePreferences = HomePreferences(context = context)
    val inboxPreferences = InboxPreferences(context = context)
    val databasePreferences = DatabasePreferences(context = context)
    val relayPreferences = RelayPreferences(context = context)
    val eventPreferences = EventPreferences(context = context)

    val keyStore = KeyStore(context = context)

    // Issue: Turn gossip on/off in setttings
    val clientOpts = Options().gossip(true).automaticAuthentication(relayPreferences.getSendAuth())
    // TODO: Admit Policy and database
    private val nostrClient =
        ClientBuilder().signer(keyStore.activeSigner()).opts(clientOpts).build()

    // TODO: Extract this for nostrClient

    private val friendProvider = FriendProvider(friendDao = roomDb.friendDao())

    val metadataInMemory = MetadataInMemory()

    private val nameProvider = NameProvider(
        profileDao = roomDb.profileDao(),
        metadataInMemory = metadataInMemory,
    )

    val annotatedStringProvider = AnnotatedStringProvider(nameProvider = nameProvider)

    private val webOfTrustProvider = WebOfTrustProvider(
        friendProvider = friendProvider,
        webOfTrustDao = roomDb.webOfTrustDao()
    )

    private val pubkeyProvider = PubkeyProvider(
        friendProvider = friendProvider,
        webOfTrustProvider = webOfTrustProvider
    )

    val itemSetProvider = ItemSetProvider(
        room = roomDb,
        friendProvider = friendProvider,
        annotatedStringProvider = annotatedStringProvider,
    )

    val topicProvider = TopicProvider(
        topicDao = roomDb.topicDao(),
        itemSetProvider = itemSetProvider,
    )

    private val eventCounter = EventCounter()

    val subCreator = SubscriptionCreator(
        nostrClient = nostrClient,
        syncedFilterCache = syncedFilterCache,
        eventCounter = eventCounter
    )

    private val filterCreator = FilterCreator(room = roomDb)

    val lazyNostrSubscriber = LazyNostrSubscriber(
        subCreator = subCreator,
        room = roomDb,
        filterCreator = filterCreator,
        webOfTrustProvider = webOfTrustProvider,
        friendProvider = friendProvider,
        topicProvider = topicProvider,
        itemSetProvider = itemSetProvider,
        pubkeyProvider = pubkeyProvider,
    )

    private val subBatcher = SubBatcher(subCreator = subCreator)

    val nostrSubscriber = NostrSubscriber(
        topicProvider = topicProvider,
        friendProvider = friendProvider,
        subCreator = subCreator,
        subBatcher = subBatcher,
        room = roomDb,
        filterCreator = filterCreator,
    )

    val accountSwitcher = AccountSwitcher(
        accountDao = roomDb.accountDao(),
        mainEventDao = roomDb.mainEventDao(),
        lazyNostrSubscriber = lazyNostrSubscriber,
        nostrSubscriber = nostrSubscriber,
        homePreferences = homePreferences,
    )

    private val eventValidator = EventValidator(
        syncedFilterCache = syncedFilterCache,
        syncedIdCache = syncedIdCache,
    )
    private val eventProcessor = EventProcessor(
        room = roomDb,
        metadataInMemory = metadataInMemory,
    )
    private val eventQueue = EventQueue(
        eventValidator = eventValidator,
        eventProcessor = eventProcessor
    )
    private val eventMaker = EventMaker(
        eventPreferences = eventPreferences,
    )

    val databaseInteractor = DatabaseInteractor(
        room = roomDb,
        context = context,
        storageHelper = storageHelper,
        snackbar = snackbar
    )

    val nostrService = NostrService(
        nostrClient = nostrClient,
        eventMaker = eventMaker,
    )

    init {
        nameProvider.lazyNostrSubscriber = lazyNostrSubscriber
        pubkeyProvider.itemSetProvider = itemSetProvider
        nostrService.initialize(initRelayUrls = relayProvider.getReadRelays())
    }

    val eventDeletor = EventDeletor(
        snackbar = snackbar,
        nostrService = nostrService,
        context = context,
        relayProvider = relayProvider,
        deleteDao = roomDb.deleteDao()
    )

    val postDetailInspector = PostDetailInspector(
        mainEventDao = roomDb.mainEventDao(),
        hashtagDao = roomDb.hashtagDao(),
    )

    val eventRebroadcaster = EventRebroadcaster(
        nostrService = nostrService,
        mainEventDao = roomDb.mainEventDao(),
        relayProvider = relayProvider,
        snackbar = snackbar,
    )

    val postVoter = PostVoter(
        nostrService = nostrService,
        relayProvider = relayProvider,
        snackbar = snackbar,
        context = context,
        voteDao = roomDb.voteDao(),
        eventDeletor = eventDeletor,
        rebroadcaster = eventRebroadcaster,
        relayPreferences = relayPreferences,
        eventPreferences = eventPreferences,
    )

    val threadCollapser = ThreadCollapser()

    val topicFollower = TopicFollower(
        nostrService = nostrService,
        relayProvider = relayProvider,
        topicUpsertDao = roomDb.topicUpsertDao(),
        topicDao = roomDb.topicDao(),
        snackbar = snackbar,
        context = context,
        forcedFollowStates = forcedFollowTopicStates
    )

    val bookmarker = Bookmarker(
        nostrService = nostrService,
        relayProvider = relayProvider,
        bookmarkUpsertDao = roomDb.bookmarkUpsertDao(),
        bookmarkDao = roomDb.bookmarkDao(),
        snackbar = snackbar,
        context = context,
        rebroadcaster = eventRebroadcaster,
        relayPreferences = relayPreferences,
    )

    private val oldestUsedEvent = OldestUsedEvent()

    val profileFollower = ProfileFollower(
        nostrService = nostrService,
        relayProvider = relayProvider,
        snackbar = snackbar,
        context = context,
        friendProvider = friendProvider,
        friendUpsertDao = roomDb.friendUpsertDao(),
    )

    val feedProvider = FeedProvider(
        nostrSubscriber = nostrSubscriber,
        room = roomDb,
        oldestUsedEvent = oldestUsedEvent,
        annotatedStringProvider = annotatedStringProvider,
        forcedVotes = postVoter.forcedVotes,
        forcedFollows = profileFollower.forcedFollowsFlow,
        forcedBookmarks = bookmarker.forcedBookmarksFlow,
    )

    val threadProvider = ThreadProvider(
        nostrSubscriber = nostrSubscriber,
        lazyNostrSubscriber = lazyNostrSubscriber,
        room = roomDb,
        collapsedIds = threadCollapser.collapsedIds,
        annotatedStringProvider = annotatedStringProvider,
        oldestUsedEvent = oldestUsedEvent,
        forcedVotes = postVoter.forcedVotes,
        forcedFollows = profileFollower.forcedFollowsFlow,
        forcedBookmarks = bookmarker.forcedBookmarksFlow,
    )

    val profileProvider = ProfileProvider(
        forcedFollowFlow = profileFollower.forcedFollowsFlow,
        myPubkeyProvider = accountManager,
        metadataInMemory = metadataInMemory,
        room = roomDb,
        friendProvider = friendProvider,
        itemSetProvider = itemSetProvider,
        lazyNostrSubscriber = lazyNostrSubscriber,
        annotatedStringProvider = annotatedStringProvider,
    )

    val searchProvider = SearchProvider(
        topicProvider = topicProvider,
        profileProvider = profileProvider,
        mainEventDao = roomDb.mainEventDao()
    )

    val suggestionProvider = SuggestionProvider(
        searchProvider = searchProvider,
        lazyNostrSubscriber = lazyNostrSubscriber,
    )

    val postSender = PostSender(
        nostrService = nostrService,
        relayProvider = relayProvider,
        mainEventInsertDao = roomDb.mainEventInsertDao(),
        mainEventDao = roomDb.mainEventDao(),
        myPubkeyProvider = accountManager,
    )

    val eventSweeper = EventSweeper(
        databasePreferences = databasePreferences,
        idCacheClearer = idCacheClearer,
        deleteDao = roomDb.deleteDao(),
        oldestUsedEvent = oldestUsedEvent
    )

    val relayProfileProvider = RelayProfileProvider()

    val itemSetEditor = ItemSetEditor(
        nostrService = nostrService,
        relayProvider = relayProvider,
        profileSetUpsertDao = roomDb.profileSetUpsertDao(),
        topicSetUpsertDao = roomDb.topicSetUpsertDao(),
        itemSetProvider = itemSetProvider,
    )
}
