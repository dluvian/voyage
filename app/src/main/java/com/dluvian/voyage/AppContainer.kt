package com.dluvian.voyage

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.room.Room
import com.anggrayudi.storage.SimpleStorageHelper
import com.dluvian.voyage.core.ExternalSignerHandler
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.ConnectionStatus
import com.dluvian.voyage.data.account.AccountManager
import com.dluvian.voyage.data.account.AccountSwitcher
import com.dluvian.voyage.data.account.ExternalSigner
import com.dluvian.voyage.data.account.MnemonicSigner
import com.dluvian.voyage.data.event.EventCounter
import com.dluvian.voyage.data.event.EventDeletor
import com.dluvian.voyage.data.event.EventMaker
import com.dluvian.voyage.data.event.EventProcessor
import com.dluvian.voyage.data.event.EventQueue
import com.dluvian.voyage.data.event.EventRebroadcaster
import com.dluvian.voyage.data.event.EventSweeper
import com.dluvian.voyage.data.event.EventValidator
import com.dluvian.voyage.data.event.IdCacheClearer
import com.dluvian.voyage.data.event.OldestUsedEvent
import com.dluvian.voyage.data.inMemory.MetadataInMemory
import com.dluvian.voyage.data.interactor.Bookmarker
import com.dluvian.voyage.data.interactor.ItemSetEditor
import com.dluvian.voyage.data.interactor.PollVoter
import com.dluvian.voyage.data.interactor.PostDetailInspector
import com.dluvian.voyage.data.interactor.PostSender
import com.dluvian.voyage.data.interactor.PostVoter
import com.dluvian.voyage.data.interactor.ProfileFollower
import com.dluvian.voyage.data.interactor.ThreadCollapser
import com.dluvian.voyage.data.interactor.TopicFollower
import com.dluvian.voyage.data.nostr.FilterCreator
import com.dluvian.voyage.data.nostr.LazyNostrSubscriber
import com.dluvian.voyage.data.nostr.NostrClient
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.nostr.NostrSubscriber
import com.dluvian.voyage.data.nostr.RelayUrl
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
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.SearchProvider
import com.dluvian.voyage.data.provider.SuggestionProvider
import com.dluvian.voyage.data.provider.ThreadProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import com.dluvian.voyage.data.room.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
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
    private val nostrClient = NostrClient()
    val mnemonicSigner = MnemonicSigner(context = context)
    val externalSignerHandler = ExternalSignerHandler()
    private val externalSigner = ExternalSigner(handler = externalSignerHandler)

    private val idCacheClearer = IdCacheClearer(
        syncedIdCache = syncedIdCache,
    )

    val connectionStatuses = mutableStateOf(mapOf<RelayUrl, ConnectionStatus>())

    private val forcedFollowTopicStates = MutableStateFlow(emptyMap<Topic, Boolean>())

    val homePreferences = HomePreferences(context = context)
    val inboxPreferences = InboxPreferences(context = context)
    val databasePreferences = DatabasePreferences(context = context)
    val relayPreferences = RelayPreferences(context = context)
    val eventPreferences = EventPreferences(context = context)

    val accountManager = AccountManager(
        mnemonicSigner = mnemonicSigner,
        externalSigner = externalSigner,
        accountDao = roomDb.accountDao(),
    )

    private val friendProvider = FriendProvider(
        friendDao = roomDb.friendDao(),
        myPubkeyProvider = accountManager,
    )

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

    val relayProvider = RelayProvider(
        nip65Dao = roomDb.nip65Dao(),
        eventRelayDao = roomDb.eventRelayDao(),
        nostrClient = nostrClient,
        connectionStatuses = connectionStatuses,
        pubkeyProvider = pubkeyProvider,
        relayPreferences = relayPreferences,
        webOfTrustProvider = webOfTrustProvider
    )

    val itemSetProvider = ItemSetProvider(
        room = roomDb,
        myPubkeyProvider = accountManager,
        friendProvider = friendProvider,
        annotatedStringProvider = annotatedStringProvider,
        relayProvider = relayProvider,
    )

    val topicProvider = TopicProvider(
        forcedFollowStates = forcedFollowTopicStates,
        topicDao = roomDb.topicDao(),
        itemSetProvider = itemSetProvider,
    )


    private val eventCounter = EventCounter()

    val subCreator = SubscriptionCreator(
        nostrClient = nostrClient,
        syncedFilterCache = syncedFilterCache,
        eventCounter = eventCounter
    )

    private val filterCreator = FilterCreator(
        room = roomDb,
        myPubkeyProvider = accountManager,
        relayProvider = relayProvider,
    )

    val lazyNostrSubscriber = LazyNostrSubscriber(
        subCreator = subCreator,
        room = roomDb,
        relayProvider = relayProvider,
        filterCreator = filterCreator,
        webOfTrustProvider = webOfTrustProvider,
        friendProvider = friendProvider,
        topicProvider = topicProvider,
        myPubkeyProvider = accountManager,
        itemSetProvider = itemSetProvider,
        pubkeyProvider = pubkeyProvider,
    )

    private val subBatcher = SubBatcher(subCreator = subCreator, myPubkeyProvider = accountManager)

    val nostrSubscriber = NostrSubscriber(
        topicProvider = topicProvider,
        myPubkeyProvider = accountManager,
        friendProvider = friendProvider,
        subCreator = subCreator,
        relayProvider = relayProvider,
        subBatcher = subBatcher,
        room = roomDb,
        filterCreator = filterCreator,
    )

    val accountSwitcher = AccountSwitcher(
        accountManager = accountManager,
        accountDao = roomDb.accountDao(),
        mainEventDao = roomDb.mainEventDao(),
        idCacheClearer = idCacheClearer,
        lazyNostrSubscriber = lazyNostrSubscriber,
        nostrSubscriber = nostrSubscriber,
        homePreferences = homePreferences,
    )

    private val eventValidator = EventValidator(
        syncedFilterCache = syncedFilterCache,
        syncedIdCache = syncedIdCache,
        myPubkeyProvider = accountManager
    )
    private val eventProcessor = EventProcessor(
        room = roomDb,
        metadataInMemory = metadataInMemory,
        myPubkeyProvider = accountManager
    )
    private val eventQueue = EventQueue(
        eventValidator = eventValidator,
        eventProcessor = eventProcessor
    )
    private val eventMaker = EventMaker(
        accountManager = accountManager,
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
        eventQueue = eventQueue,
        eventMaker = eventMaker,
        filterCache = syncedFilterCache,
        relayPreferences = relayPreferences,
        connectionStatuses = connectionStatuses,
        eventCounter = eventCounter,
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

    val pollVoter = PollVoter(
        nostrService = nostrService,
        relayProvider = relayProvider,
        snackbar = snackbar,
        context = context,
        pollResponseDao = roomDb.pollResponseDao(),
        pollDao = roomDb.pollDao(),
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
