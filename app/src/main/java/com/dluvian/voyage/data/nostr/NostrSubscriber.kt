package com.dluvian.voyage.data.nostr

import com.dluvian.nostr_kt.removeTrailingSlashes
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.RESUB_TIMEOUT
import com.dluvian.voyage.core.createReplyAndVoteFilters
import com.dluvian.voyage.core.model.IParentUI
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.model.FeedSetting
import com.dluvian.voyage.data.model.HomeFeedSetting
import com.dluvian.voyage.data.model.ProfileFeedSetting
import com.dluvian.voyage.data.model.TopicFeedSetting
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Nip19Event
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

class NostrSubscriber(
    topicProvider: TopicProvider,
    val subCreator: SubscriptionCreator,
    private val friendProvider: FriendProvider,
    private val relayProvider: RelayProvider,
    private val webOfTrustProvider: WebOfTrustProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val subBatcher: SubBatcher,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

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
                // Smaller than adjustedLimit, bc posts with topics tend to be root
                limit = (2.5 * limit).toULong()
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

    // No lazySubProfile bc we always don't save fields in db
    suspend fun subProfile(nprofile: Nip19Profile) {
        val profileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
            .author(author = nprofile.publicKey())
            .until(timestamp = Timestamp.now())
            .limit(1u)
        val filters = listOf(profileFilter)

        relayProvider.getObserveRelays(nprofile = nprofile).forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private val votesAndRepliesCache = mutableSetOf<EventIdHex>()
    private var lastUpdate = System.currentTimeMillis()
    private val isSubbingVotesAndReplies = AtomicBoolean(false)
    suspend fun subVotesAndReplies(posts: Collection<IParentUI>) {
        if (posts.isEmpty()) return

        if (isSubbingVotesAndReplies.compareAndSet(false, true)) {
            scope.launch(Dispatchers.Default) {
                val currentMillis = System.currentTimeMillis()
                if (currentMillis - lastUpdate > RESUB_TIMEOUT) {
                    votesAndRepliesCache.clear()
                    lastUpdate = currentMillis
                }

                val newIds = posts.map { it.id } - votesAndRepliesCache
                if (newIds.isEmpty()) return@launch


                votesAndRepliesCache.addAll(newIds)

                val newPostsByAuthor = posts.filter { newIds.contains(it.id) }.groupBy { it.pubkey }
                // Repliers and voters publish to authors read relays
                val relaysByAuthor = relayProvider.getReadRelays(pubkeys = newPostsByAuthor.keys)
                val myReadRelays = relayProvider.getReadRelays().toSet()
                val votePubkeys = getVotePubkeys()

                relaysByAuthor.forEach { (author, relays) ->
                    val adjustedRelays = myReadRelays + relays
                    adjustedRelays.forEach { relay ->
                        subBatcher.submitVotesAndReplies(
                            relayUrl = relay,
                            eventIds = newPostsByAuthor[author]?.map { it.id } ?: emptyList(),
                            votePubkeys = votePubkeys
                        )
                    }
                }
            }.invokeOnCompletion {
                isSubbingVotesAndReplies.set(false)
            }
        }
    }

    fun subVotesAndReplies(nevent: Nip19Event) {
        val filters = createReplyAndVoteFilters(
            ids = listOf(nevent.eventId()),
            votePubkeys = getVotePubkeys().map { PublicKey.fromHex(it) },
            timestamp = Timestamp.now()
        )

        nevent.relays()
            .map { it.removeTrailingSlashes() }
            .toSet() + relayProvider.getReadRelays()
            .forEach { relay ->
                subCreator.subscribe(relayUrl = relay, filters = filters)
            }
    }

    private fun getVotePubkeys(): List<PubkeyHex> {
        val pubkeys = mutableSetOf(pubkeyProvider.getPubkeyHex())
        pubkeys.addAll(friendProvider.getFriendPubkeys())
        pubkeys.addAll(webOfTrustProvider.getWebOfTrustPubkeys())

        return pubkeys.toList()
    }
}
