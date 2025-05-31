package com.dluvian.voyage

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import com.dluvian.voyage.preferences.EventPreferences
import com.dluvian.voyage.preferences.HomePreferences
import com.dluvian.voyage.preferences.InboxPreferences
import com.dluvian.voyage.preferences.RelayPreferences
import com.dluvian.voyage.provider.AnnotatedStringProvider
import com.dluvian.voyage.provider.BookmarkProvider
import com.dluvian.voyage.provider.NameProvider
import com.dluvian.voyage.provider.TopicProvider
import com.dluvian.voyage.provider.TrustProvider

class AppContainer(val context: Context) {
    val snackbar = SnackbarHostState()

    val homePreferences = HomePreferences(context = context)
    val inboxPreferences = InboxPreferences(context = context)
    val relayPreferences = RelayPreferences(context = context)
    val eventPreferences = EventPreferences(context = context)

    val keyStore = KeyStore()
    val oldestUsedEvent = OldestUsedEvent()

    val nameProvider = NameProvider()
    val annotatedStringProvider = AnnotatedStringProvider(nameProvider = nameProvider)

    val service = NostrService(
        relayPreferences = relayPreferences,
        keyStore = keyStore
    )

    val trustProvider = TrustProvider(service)
    val topicProvider = TopicProvider(service)
    val bookmarkProvider = BookmarkProvider(service)

    val eventCreator = EventCreator(
        service,
        eventPreferences,
        trustProvider,
        topicProvider,
        bookmarkProvider
    )

    val dbSweeper = DatabaseSweeper(service, oldestUsedEvent)

    val threadCollapser = ThreadCollapser()
}