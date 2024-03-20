package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.core.DEBOUNCE
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.RND_RESUB_COUNT
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.FilterWrapper
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

class NostrSubscriber(
    topicProvider: TopicProvider,
    private val relayProvider: RelayProvider,
    private val webOfTrustProvider: WebOfTrustProvider,
    private val friendProvider: FriendProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrClient: NostrClient,
    private val syncedFilterCache: MutableMap<SubId, List<FilterWrapper>>,
) {
    private val tag = "NostrSubscriber"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val feedSubscriber = NostrFeedSubscriber(
        scope = scope,
        relayProvider = relayProvider,
        topicProvider = topicProvider,
        friendProvider = friendProvider
    )

    suspend fun subFeed(until: Long, limit: Int, setting: FeedSetting) {
        val untilTimestamp = Timestamp.fromSecs(until.toULong())
        val adjustedLimit = (5L * limit).toULong() // We don't know if we receive enough root posts

        val subscriptions = when (setting) {
            is HomeFeedSetting -> feedSubscriber.getHomeFeedSubscriptions(
                until = untilTimestamp,
                limit = adjustedLimit
            )

            is TopicFeedSetting -> feedSubscriber.getTopicFeedSubscription(
                topic = setting.topic,
                until = untilTimestamp,
                limit = adjustedLimit
            )

            is ProfileFeedSetting -> feedSubscriber.getProfileFeedSubscription(
                pubkey = setting.pubkey,
                until = untilTimestamp,
                limit = adjustedLimit
            )
        }

        subscriptions.forEach { (relay, filters) ->
            subscribe(relayUrl = relay, filters = filters)
        }
    }

    // TODO: remove ids after x seconds to enable resubbing
    private val votesAndRepliesCache = mutableSetOf<EventIdHex>()
    private var votesAndRepliesJob: Job? = null
    fun subVotesAndReplies(postIds: Collection<EventIdHex>) {
        if (postIds.isEmpty()) return

        val newIds = postIds - votesAndRepliesCache
        if (newIds.isEmpty()) return

        votesAndRepliesJob?.cancel(CancellationException("Debounce"))
        votesAndRepliesJob = scope.launch {
            delay(DEBOUNCE)
            val currentTimestamp = Timestamp.now()
            val ids = newIds.map { EventId.fromHex(it) }
            val voteFilter = Filter().kind(Kind.fromEnum(KindEnum.Reaction))
                .events(ids)
                .authors(webOfTrustProvider.getWebOfTrustPubkeys())
                .until(currentTimestamp)
                .limit(MAX_EVENTS_TO_SUB.toULong())
            val replyFilter = Filter().kind(Kind.fromEnum(KindEnum.TextNote))
                .events(ids)
                .until(currentTimestamp)
                .limit(MAX_EVENTS_TO_SUB.toULong())
            val filters = listOf(
                FilterWrapper(filter = voteFilter),
                FilterWrapper(filter = replyFilter, e = ids.map { it.toHex() })
            )

            relayProvider.getReadRelays().forEach { relay ->
                subscribe(relayUrl = relay, filters = filters)
            }
        }
        votesAndRepliesJob?.invokeOnCompletion { ex ->
            if (ex != null) return@invokeOnCompletion

            votesAndRepliesCache.addAll(newIds)
            Log.d(tag, "Finished subscribing votes and replies")
        }
    }

    fun subProfile(nip19Profile: Nip19Profile) {
        val profileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
            .author(author = nip19Profile.publicKey())
            .until(timestamp = Timestamp.now())
            .limit(1u)
        val filters = listOf(FilterWrapper(profileFilter))

        relayProvider.getObserveRelays(nip19Profile = nip19Profile).forEach { relay ->
            subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun subMyAccountAndTrustData() {
        Log.d(tag, "subMyAccountAndTrustData")
        subMyAccount()
        delay(DELAY_1SEC) // TODO: Channel wait instead of delay
        lazySubFriendsNip65()
        delay(DELAY_1SEC)
        lazySubWebOfTrustPubkeys()
    }

    suspend fun lazySubWebOfTrustProfiles() {
        val webOfTrustWithMissingProfiles = webOfTrustProvider.getWotWithMissingProfiles()
        val randomResub = webOfTrustProvider.getWebOfTrustPubkeys().shuffled().take(RND_RESUB_COUNT)
        val webOfTrustResub = (webOfTrustWithMissingProfiles + randomResub).distinct()
        if (webOfTrustResub.isEmpty()) return

        val timestamp = Timestamp.now()
        relayProvider
            .getObserveRelays(observeFrom = webOfTrustWithMissingProfiles)
            .forEach { (relay, pubkeys) ->
                val profileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
                    .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                val filters = listOf(FilterWrapper(profileFilter))
                subscribe(relayUrl = relay, filters = filters)
            }
    }

    private fun subMyAccount() {
        val timestamp = Timestamp.now()
        val myPubkey = pubkeyProvider.getPublicKey()
        val myContactFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.ContactList))
            .author(author = myPubkey)
            .until(timestamp = timestamp)
            .limit(1u)
        val myTopicsFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Interests))
            .author(author = myPubkey)
            .until(timestamp = timestamp)
            .limit(1u)
        val myNip65Filter = Filter().kind(kind = Kind.fromEnum(KindEnum.RelayList))
            .author(author = myPubkey)
            .until(timestamp = timestamp)
            .limit(1u)
        val myProfileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
            .author(author = myPubkey)
            .until(timestamp = timestamp)
            .limit(1u)
        val filters = listOf(
            FilterWrapper(myContactFilter),
            FilterWrapper(myTopicsFilter),
            FilterWrapper(myNip65Filter),
            FilterWrapper(myProfileFilter),
        )

        relayProvider.getReadRelays().forEach { relay ->
            subscribe(relayUrl = relay, filters = filters)
        }
    }

    private suspend fun lazySubFriendsNip65() {
        val friendsWithMissingNip65 = friendProvider.getFriendsWithMissingNip65()
        val randomResub = friendProvider.getFriendPubkeys().shuffled().take(RND_RESUB_COUNT)
        val nip65Resub = (friendsWithMissingNip65 + randomResub).distinct()
        if (nip65Resub.isEmpty()) return

        val timestamp = Timestamp.now()
        relayProvider
            .getObserveRelays(observeFrom = nip65Resub)
            .forEach { (relay, pubkeys) ->
                val nip65Filter = Filter().kind(kind = Kind.fromEnum(KindEnum.RelayList))
                    .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                val filters = listOf(FilterWrapper(nip65Filter))
                subscribe(relayUrl = relay, filters = filters)
            }
    }

    private suspend fun lazySubWebOfTrustPubkeys() {
        val friendsWithMissingContactList = friendProvider.getFriendsWithMissingContactList()
        val randomResub = friendProvider.getFriendPubkeys().shuffled().take(RND_RESUB_COUNT)
        val webOfTrustResub = (friendsWithMissingContactList + randomResub).distinct()
        if (webOfTrustResub.isEmpty()) return

        val timestamp = Timestamp.now()
        relayProvider
            .getObserveRelays(observeFrom = webOfTrustResub)
            .forEach { (relay, pubkeys) ->
                val webOfTrustFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.ContactList))
                    .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                val filters = listOf(FilterWrapper(webOfTrustFilter))
                subscribe(relayUrl = relay, filters = filters)
            }
    }

    private fun subscribe(relayUrl: RelayUrl, filters: List<FilterWrapper>): SubId? {
        if (filters.isEmpty()) return null
        Log.d(tag, "Subscribe ${filters.size} in $relayUrl")

        val subId = nostrClient.subscribe(filters = filters.map { it.filter }, relayUrl = relayUrl)
        if (subId == null) {
            Log.w(tag, "Failed to create subscription ID")
            return null
        }
        syncedFilterCache[subId] = filters

        return subId
    }
}
