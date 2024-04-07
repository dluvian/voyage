package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.removeTrailingSlashes
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.RESUB_TIMEOUT
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

private const val TAG = "NostrSubscriber"

class NostrSubscriber(
    topicProvider: TopicProvider,
    friendProvider: FriendProvider,
    private val relayProvider: RelayProvider,
    private val webOfTrustProvider: WebOfTrustProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val subCreator: SubscriptionCreator,
    private val lazyNostrSubscriber: LazyNostrSubscriber,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val subDebouncer = SubDebouncer(subCreator = subCreator)

    private val feedSubscriber = NostrFeedSubscriber(
        scope = scope,
        relayProvider = relayProvider,
        topicProvider = topicProvider,
        friendProvider = friendProvider
    )

    suspend fun subFeed(until: Long, limit: Int, setting: FeedSetting) {
        val untilTimestamp = Timestamp.fromSecs(until.toULong())
        val adjustedLimit = (4 * limit).toULong() // We don't know if we receive enough root posts

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
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private val votesAndRepliesCache = mutableSetOf<EventIdHex>()
    private var lastUpdate = System.currentTimeMillis()
    fun subVotesAndReplies(postIds: Collection<EventIdHex>) {
        if (postIds.isEmpty()) return

        synchronized(votesAndRepliesCache) {
            val newIds = postIds - votesAndRepliesCache
            if (newIds.isEmpty()) return

            val ids = newIds.map { EventId.fromHex(it) }
            val filters = createReplyAndVoteFilters(ids = ids)
            relayProvider.getReadRelays().forEach { relay ->
                subDebouncer.submit(relayUrl = relay, filters = filters)
            }
            val currentMillis = System.currentTimeMillis()
            if (currentMillis - lastUpdate > RESUB_TIMEOUT) votesAndRepliesCache.clear()
            lastUpdate = currentMillis

            votesAndRepliesCache.addAll(newIds)
        }
    }

    fun subVotesAndReplies(nevent: Nip19Event) {
        val filters = createReplyAndVoteFilters(ids = listOf(nevent.eventId()))

        nevent.relays()
            .map { it.removeTrailingSlashes() }
            .toSet() + relayProvider.getReadRelays()
            .forEach { relay ->
                subCreator.subscribe(relayUrl = relay, filters = filters)
            }
    }

    suspend fun subProfile(nprofile: Nip19Profile) {
        val profileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
            .author(author = nprofile.publicKey())
            .until(timestamp = Timestamp.now())
            .limit(1u)
        val filters = listOf(FilterWrapper(profileFilter))

        relayProvider.getObserveRelays(nprofile = nprofile).forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun subMyAccountAndTrustData() {
        Log.d(TAG, "subMyAccountAndTrustData")
        subMyAccount()
        delay(DELAY_1SEC)
        lazyNostrSubscriber.semiLazySubFriendsNip65()
        delay(DELAY_1SEC)
        lazyNostrSubscriber.semiLazySubWebOfTrustPubkeys()
    }

    suspend fun subNip65(pubkey: PubkeyHex) {
        val nip65Filter = Filter().kind(kind = Kind.fromEnum(KindEnum.RelayList))
            .author(author = PublicKey.fromHex(pubkey))
            .until(timestamp = Timestamp.now())
            .limit(1u)
        val filters = listOf(FilterWrapper(nip65Filter))
        relayProvider.getObserveRelays(pubkey = pubkey, includeConnected = true).forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
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
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private fun createReplyAndVoteFilters(ids: List<EventId>): List<FilterWrapper> {
        val now = Timestamp.now()
        val voteFilter = Filter().kind(Kind.fromEnum(KindEnum.Reaction))
            .events(ids = ids)
            .authors(authors = webOfTrustProvider.getWebOfTrustPubkeys())
            .until(timestamp = now)
            .limit(limit = MAX_EVENTS_TO_SUB)
        val replyFilter = Filter().kind(Kind.fromEnum(KindEnum.TextNote))
            .events(ids = ids)
            .until(timestamp = now)
            .limit(limit = MAX_EVENTS_TO_SUB)

        return listOf(
            FilterWrapper(filter = voteFilter),
            FilterWrapper(filter = replyFilter, e = ids.map { it.toHex() })
        )
    }
}
