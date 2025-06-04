package com.dluvian.voyage

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import com.dluvian.voyage.model.RelayNotificationCmd
import com.dluvian.voyage.nostr.EventCreator
import com.dluvian.voyage.nostr.NostrService
import com.dluvian.voyage.preferences.EventPreferences
import com.dluvian.voyage.preferences.HomePreferences
import com.dluvian.voyage.preferences.InboxPreferences
import com.dluvian.voyage.preferences.RelayPreferences
import com.dluvian.voyage.provider.AnnotatedStringProvider
import com.dluvian.voyage.provider.BookmarkProvider
import com.dluvian.voyage.provider.FeedProvider
import com.dluvian.voyage.provider.IEventUpdate
import com.dluvian.voyage.provider.NameProvider
import com.dluvian.voyage.provider.ProfileProvider
import com.dluvian.voyage.provider.TopicProvider
import com.dluvian.voyage.provider.TrustProvider
import com.dluvian.voyage.provider.UpvoteProvider
import kotlinx.coroutines.channels.Channel

class AppContainer(val context: Context) {
    val snackbar = SnackbarHostState()

    val homePreferences = HomePreferences(context)
    val inboxPreferences = InboxPreferences(context)
    val relayPreferences = RelayPreferences(context)
    val eventPreferences = EventPreferences(context)

    val keyStore = KeyStore()
    val oldestUsedEvent = OldestUsedEvent()
    val relayChannel = Channel<RelayNotificationCmd>(capacity = Channel.Factory.UNLIMITED)

    val service = NostrService(relayPreferences, keyStore, relayChannel)

    val profileProvider = ProfileProvider(service)
    val nameProvider = NameProvider(service)
    val trustProvider = TrustProvider(service)
    val topicProvider = TopicProvider(service)
    val bookmarkProvider = BookmarkProvider(service)
    val upvoteProvider = UpvoteProvider(service)
    val annotatedStringProvider = AnnotatedStringProvider(nameProvider)
    val feedProvider = FeedProvider(
        service = service,
        topicProvider = topicProvider,
        trustProvider = trustProvider,
        bookmarkProvider = bookmarkProvider,
        nameProvider = nameProvider,
        upvoteProvider = upvoteProvider,
        annotatedStringProvider = annotatedStringProvider,
        oldestUsedEvent = oldestUsedEvent
    )

    val eventCreator = EventCreator(
        service,
        eventPreferences,
        trustProvider,
        topicProvider,
        bookmarkProvider
    )

    val dbSweeper = DatabaseSweeper(service, oldestUsedEvent)

    fun getEventUpdatables(): List<IEventUpdate> {
        return listOf(nameProvider, trustProvider, topicProvider, bookmarkProvider, upvoteProvider)
    }
}