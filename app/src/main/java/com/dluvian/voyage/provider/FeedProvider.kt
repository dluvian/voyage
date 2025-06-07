package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.Topic
import com.dluvian.voyage.filterSetting.BookmarkFeedSetting
import com.dluvian.voyage.filterSetting.FeedSetting
import com.dluvian.voyage.filterSetting.FriendPubkeys
import com.dluvian.voyage.filterSetting.Global
import com.dluvian.voyage.filterSetting.HomeFeedSetting
import com.dluvian.voyage.filterSetting.InboxFeedSetting
import com.dluvian.voyage.filterSetting.ListFeedSetting
import com.dluvian.voyage.filterSetting.NoPubkeys
import com.dluvian.voyage.filterSetting.ProfileFeedSetting
import com.dluvian.voyage.filterSetting.TopicFeedSetting
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.model.UIEvent
import com.dluvian.voyage.model.UnknownProfile
import com.dluvian.voyage.nostr.NostrService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.Timestamp

class FeedProvider(
    private val service: NostrService,
    private val topicProvider: TopicProvider,
    private val trustProvider: TrustProvider,
    private val bookmarkProvider: BookmarkProvider,
    private val nameProvider: NameProvider,
    private val upvoteProvider: UpvoteProvider,
    private val annotator: AnnotatedStringProvider,
    private val oldestUsedTimestampProvider: OldestUsedTimestampProvider,
) {
    private val logTag = "FeedProvider"
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun buildFeed(
        until: Timestamp,
        setting: FeedSetting,
        dbOnly: Boolean = false,
    ): List<UIEvent> {
        return when (setting) {
            is HomeFeedSetting -> homeFeed(until, setting, dbOnly)
            is TopicFeedSetting -> topicFeed(until, setting, dbOnly)
            is ProfileFeedSetting -> profileFeed(until, setting, dbOnly)
            is InboxFeedSetting -> inboxFeed(until, setting, dbOnly)
            is ListFeedSetting -> listFeed(until, setting, dbOnly)
            is BookmarkFeedSetting -> bookmarkFeed(until, setting, dbOnly)
        }
    }

    private suspend fun homeFeed(
        until: Timestamp,
        setting: HomeFeedSetting,
        dbOnly: Boolean
    ): List<UIEvent> {
        val primaryFeedFilter = when (setting.pubkeySelection) {
            FriendPubkeys -> {
                val friends = trustProvider.friends()
                if (friends.isEmpty()) null else Filter().authors(friends)
            }
            Global -> Filter()
            NoPubkeys -> null
        }

        val topicFeedFilter = if (setting.withTopics) {
            val topics = topicProvider.topics()
            if (topics.isEmpty()) null else Filter().hashtags(topics)
        } else null

        val filters = listOfNotNull(primaryFeedFilter, topicFeedFilter)
            .map { it.kinds(setting.kinds).until(until).limit(setting.pageSize) }

        return buildFeedFromFilters(
            filters = filters,
            pageSize = setting.pageSize.toInt(),
            dbOnly = dbOnly
        )
    }

    private suspend fun topicFeed(
        until: Timestamp,
        setting: TopicFeedSetting,
        dbOnly: Boolean
    ): List<UIEvent> {
        val filter = Filter().hashtag(setting.topic)
            .kinds(setting.kinds)
            .until(until)
            .limit(setting.pageSize)

        return buildFeedFromFilters(
            filters = listOf(filter),
            pageSize = setting.pageSize.toInt(),
            dbOnly = dbOnly
        )
    }

    private suspend fun profileFeed(
        until: Timestamp,
        setting: ProfileFeedSetting,
        dbOnly: Boolean
    ): List<UIEvent> {
        val filter = Filter().author(setting.pubkey)
            .kinds(setting.kinds)
            .until(until)
            .limit(setting.pageSize)

        return buildFeedFromFilters(
            filters = listOf(filter),
            pageSize = setting.pageSize.toInt(),
            dbOnly = dbOnly
        )
    }

    private suspend fun inboxFeed(
        until: Timestamp,
        setting: InboxFeedSetting,
        dbOnly: Boolean
    ): List<UIEvent> {
        val filter = when (setting.pubkeySelection) {
            FriendPubkeys -> Filter().authors(trustProvider.friends())
            Global, NoPubkeys -> Filter()
        }
            .pubkey(service.pubkey())
            .kinds(setting.kinds)
            .until(until)
            .limit(setting.pageSize)

        return buildFeedFromFilters(
            filters = listOf(filter),
            pageSize = setting.pageSize.toInt(),
            dbOnly = dbOnly
        )
    }

    private suspend fun listFeed(
        until: Timestamp,
        setting: ListFeedSetting,
        dbOnly: Boolean
    ): List<UIEvent> {
        val kinds = listOf(KindStandard.FOLLOW_SET, KindStandard.INTEREST_SET)
            .map { Kind.fromStd(it) }
        val dbListFilter = Filter().author(service.pubkey()).kinds(kinds).identifier(setting.ident)
        val lists = service.dbQuery(dbListFilter)
        if (lists.isEmpty()) {
            Log.w(logTag, "No list with identifier '${setting.ident}' found")
            return emptyList()
        }

        val hashtags = lists.firstOrNull { it.kind().asStd() == KindStandard.INTEREST_SET }
            ?.tags()
            ?.hashtags()
            .orEmpty()
        val pubkeys = lists.firstOrNull { it.kind().asStd() == KindStandard.FOLLOW_SET }
            ?.tags()
            ?.publicKeys()
            .orEmpty()
        if (hashtags.isEmpty() && pubkeys.isEmpty()) {
            Log.i(logTag, "Lists with identifier '${setting.ident}' are empty")
            return emptyList()
        }

        val filters = mutableListOf<Filter>()
        if (hashtags.isNotEmpty()) {
            filters.add(Filter().hashtags(hashtags))
        }
        if (pubkeys.isNotEmpty()) {
            filters.add(Filter().authors(pubkeys))
        }
        val enrichedFilters = filters.map {
            it.kinds(setting.kinds).until(until).limit(setting.pageSize)
        }

        return buildFeedFromFilters(
            filters = enrichedFilters,
            pageSize = setting.pageSize.toInt(),
            dbOnly = dbOnly
        )
    }

    private suspend fun bookmarkFeed(
        until: Timestamp,
        setting: BookmarkFeedSetting,
        dbOnly: Boolean
    ): List<UIEvent> {
        val filter = Filter()
            .ids(bookmarkProvider.bookmarks())
            .until(until)
            .limit(setting.pageSize)

        return buildFeedFromFilters(
            filters = listOf(filter),
            pageSize = setting.pageSize.toInt(),
            dbOnly = dbOnly
        )
    }

    private suspend fun buildFeedFromFilters(
        filters: List<Filter>,
        pageSize: Int,
        dbOnly: Boolean
    ): List<UIEvent> {
        if (filters.isEmpty()) {
            Log.i(logTag, "No filter provided for building a feed")
            return emptyList()
        }

        val feed = mutableListOf<Event>()
        for (filter in filters) {
            feed.addAll(service.dbQuery(filter))
        }

        val oldest = feed.minByOrNull { it.createdAt().asSecs() }?.createdAt()
        oldestUsedTimestampProvider.updateCreatedAt(oldest)

        val orderedFeed = feed.distinctBy { it.id() }
            .sortedBy { it.id().toHex() } // Use pow to decide order when createdAt is same
            .sortedByDescending { it.createdAt().asSecs() } // Newest first
            .take(pageSize.toInt())

        val since = if (orderedFeed.size >= pageSize) {
            val secs = orderedFeed.minBy { it.createdAt().asSecs() }.createdAt().asSecs()
            Timestamp.fromSecs(secs + 1u) // Don't resub the last item of a full page
        } else {
            Timestamp.fromSecs(1u)
        }

        if (!dbOnly) {
            for (filter in filters) {
                scope.launch {
                    service.sync(filter.since(since))
                }
            }
        }

        return enrichEvents(events = orderedFeed, dbOnly = dbOnly)
    }

    private suspend fun enrichEvents(events: Collection<Event>, dbOnly: Boolean): List<UIEvent> {
        if (events.isEmpty()) return emptyList()

        val repostedEvents = events.filter { isRepost(it) }
            .mapNotNull { runCatching { Event.fromJson(it.content()) }.getOrNull() }
        val allEvents = events + repostedEvents
        val mentionedPubkeys = allEvents.flatMap { it.tags().publicKeys() }.toSet()
        val authorPubkeys = allEvents.map { it.author() }.toSet()
        val eventIds = allEvents.map { it.id() }.toSet()

        nameProvider.reserve(pubkeys = authorPubkeys + mentionedPubkeys, dbOnly = dbOnly)
        trustProvider.reserveWeb(pubkeys = authorPubkeys, dbOnly = dbOnly)
        upvoteProvider.reserveUpvotes(postIds = eventIds, dbOnly = dbOnly)

        val upvotes = upvoteProvider.filterUpvoted(eventIds).toSet()
        val bookmarks = bookmarkProvider.bookmarks().toSet()
        val names = nameProvider.names(authorPubkeys)
        val topics = topicProvider.topics().toSet()
        val trustProfiles = trustProvider.getTrustProfiles(pubkeys = authorPubkeys)
        trustProfiles.forEach { (_, profile) ->
            val name = names[profile.pubkey].orEmpty()
            profile.setRawName(name)
        }

        return events.map { event ->
            enrichEvent(
                event = event,
                trustProfiles = trustProfiles,
                upvotes = upvotes,
                bookmarks = bookmarks,
                topics = topics,
                repostedEvents = repostedEvents
            )
        }
    }

    fun enrichEvent(
        event: Event,
        trustProfiles: Map<PublicKey, TrustProfile>,
        upvotes: Collection<EventId>,
        bookmarks: Collection<EventId>,
        topics: Collection<Topic>,
        repostedEvents: Collection<Event>
    ): UIEvent {
        val hashtags = event.tags().hashtags().toSet()
        val innerEvent = if (isRepost(event)) {
            repostedEvents.firstOrNull { it.id() == event.tags().eventIds().firstOrNull() }
        } else null

        return UIEvent(
            event = event,
            annotatedContent = annotator.annotate(event.content()),
            authorProfile = trustProfiles[event.author()]
                ?: UnknownProfile(pubkey = event.author()),
            upvoted = upvotes.contains(event.id()),
            bookmarked = bookmarks.contains(event.id()),
            myTopic = topics.firstOrNull { hashtags.contains(it) },
            inner = if (innerEvent != null) enrichEvent(
                event = innerEvent,
                trustProfiles = trustProfiles,
                upvotes = upvotes,
                bookmarks = bookmarks,
                topics = topics,
                repostedEvents = repostedEvents
            )
            else null
        )
    }

    private fun isRepost(event: Event): Boolean {
        return event.kind() == Kind.fromStd(KindStandard.REPOST)
                || event.kind() == Kind.fromStd(KindStandard.GENERIC_REPOST)
    }
}
