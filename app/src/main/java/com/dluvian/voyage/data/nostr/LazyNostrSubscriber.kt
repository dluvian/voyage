package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.MAX_PUBKEYS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.mergeRelayFilters
import com.dluvian.voyage.core.takeRandom
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import com.dluvian.voyage.data.room.dao.ProfileDao
import kotlinx.coroutines.delay
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

private const val TAG = "LazyNostrSubscriber"

class LazyNostrSubscriber(
    private val profileDao: ProfileDao,
    private val relayProvider: RelayProvider,
    val subCreator: SubscriptionCreator,
    private val webOfTrustProvider: WebOfTrustProvider,
    private val friendProvider: FriendProvider,
    private val topicProvider: TopicProvider,
    private val pubkeyProvider: IPubkeyProvider,
) {
    suspend fun lazySubMyAccountAndTrustData() {
        Log.d(TAG, "subMyAccountAndTrustData")
        lazySubMyAccount()
        delay(DELAY_1SEC)
        semiLazySubFriendsNip65()
        delay(DELAY_1SEC)
        lazySubWebOfTrustPubkeys()
    }

    suspend fun lazySubUnknownProfiles(pubkeys: Collection<PubkeyHex>) {
        if (pubkeys.isEmpty()) return
        val unknownPubkeys = pubkeys - profileDao.filterKnownProfiles(pubkeys = pubkeys).toSet()
        if (unknownPubkeys.isEmpty()) return

        val limitedPubkeys = unknownPubkeys.takeRandom(MAX_PUBKEYS)

        val timestamp = Timestamp.now()

        relayProvider.getObserveRelays(pubkeys = limitedPubkeys).forEach { (relay, pubkeyBatch) ->
            val profileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
                .authors(authors = pubkeyBatch.map { PublicKey.fromHex(it) })
                .until(timestamp = timestamp)
            val filters = listOf(profileFilter)
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubUnknownProfiles() {
        val pubkeys = mutableListOf<PubkeyHex>()
        pubkeys.addAll(friendProvider.getFriendsWithMissingProfile())
        pubkeys.addAll(webOfTrustProvider.getWotWithMissingProfile())
        val toSub = pubkeys.distinct().take(MAX_PUBKEYS)
        if (toSub.isEmpty()) return

        val timestamp = Timestamp.now()

        relayProvider.getObserveRelays(pubkeys = toSub).forEach { (relay, pubkeyBatch) ->
            val profileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
                .authors(authors = pubkeyBatch.map { PublicKey.fromHex(it) })
                .until(timestamp = timestamp)
            val filters = listOf(profileFilter)
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubNip65(nprofile: Nip19Profile) {
        val since = relayProvider.getCreatedAt(pubkey = nprofile.publicKey().toHex()) ?: 1
        val nip65Filter = Filter().kind(kind = Kind.fromEnum(KindEnum.RelayList))
            .author(author = nprofile.publicKey())
            .until(timestamp = Timestamp.now())
            .since(timestamp = Timestamp.fromSecs((since + 1).toULong()))
            .limit(1u)
        val filters = listOf(nip65Filter)
        relayProvider.getObserveRelays(nprofile = nprofile, includeConnected = true)
            .forEach { relay -> subCreator.subscribe(relayUrl = relay, filters = filters) }
    }

    suspend fun lazySubMyAccount() {
        val timestamp = Timestamp.now()
        val pubkey = pubkeyProvider.getPublicKey()
        val hex = pubkey.toHex()

        val contactSince = friendProvider.getCreatedAt()?.toULong() ?: 1uL
        val myContactFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.ContactList))
            .author(author = pubkey)
            .until(timestamp = timestamp)
            .since(timestamp = Timestamp.fromSecs(secs = contactSince + 1u))
            .limit(1u)

        val topicSince = topicProvider.getCreatedAt()?.toULong() ?: 1uL
        val myTopicsFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Interests))
            .author(author = pubkey)
            .until(timestamp = timestamp)
            .since(timestamp = Timestamp.fromSecs(secs = topicSince + 1u))
            .limit(1u)

        val nip65Since = relayProvider.getCreatedAt(pubkey = hex)?.toULong() ?: 1uL
        val myNip65Filter = Filter().kind(kind = Kind.fromEnum(KindEnum.RelayList))
            .author(author = pubkey)
            .until(timestamp = timestamp)
            .since(timestamp = Timestamp.fromSecs(secs = nip65Since + 1u))
            .limit(1u)

        val profileSince = profileDao.getMaxCreatedAt(pubkey = hex)?.toULong() ?: 1uL
        val myProfileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
            .author(author = pubkey)
            .until(timestamp = timestamp)
            .since(timestamp = Timestamp.fromSecs(secs = profileSince + 1u))
            .limit(1u)

        val filters = listOf(myContactFilter, myTopicsFilter, myNip65Filter, myProfileFilter)

        relayProvider.getReadRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private suspend fun semiLazySubFriendsNip65() {
        val missingPubkeys = friendProvider.getFriendsWithMissingNip65()
        Log.d(TAG, "Missing nip65s of ${missingPubkeys.size} pubkeys")
        // Don't return early. We need to call lazySubNewestFriendNip65 later

        val timestamp = Timestamp.now()
        val missingSubs = relayProvider
            .getObserveRelays(pubkeys = missingPubkeys)
            .mapValues { (_, pubkeys) ->
                listOf(
                    Filter().kind(kind = Kind.fromEnum(KindEnum.RelayList))
                        .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                        .until(timestamp = timestamp)
                        .limit(pubkeys.size.toULong())
                )
            }
        val newestSubs = lazySubNewestFriendNip65(until = timestamp)

        mergeRelayFilters(missingSubs, newestSubs).forEach { (relay, filters) ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private suspend fun lazySubNewestFriendNip65(until: Timestamp): Map<RelayUrl, List<Filter>> {
        val newestCreatedAt = relayProvider.getNewestCreatedAt()?.toULong() ?: return emptyMap()
        if (newestCreatedAt >= until.asSecs()) return emptyMap()

        val friendPubkeys = friendProvider.getFriendPubkeys().map { PublicKey.fromHex(it) }
        val newNip65Filter = listOf(
            Filter().kind(kind = Kind.fromEnum(KindEnum.RelayList))
                .authors(authors = friendPubkeys)
                .until(timestamp = until)
                .since(timestamp = Timestamp.fromSecs(newestCreatedAt + 1u))
                .limit(friendPubkeys.size.toULong())
        )

        return relayProvider.getReadRelays(includeConnected = true)
            .associateWith { newNip65Filter }
    }

    private suspend fun lazySubWebOfTrustPubkeys() {
        val friendsWithMissingContacts = friendProvider.getFriendsWithMissingContactList()
        Log.d(TAG, "Missing contact lists of ${friendsWithMissingContacts.size} pubkeys")
        // Don't return early. We need to call lazySubNewestWotPubkeys later

        val timestamp = Timestamp.now()
        val missingSubs = relayProvider
            .getObserveRelays(pubkeys = friendsWithMissingContacts)
            .mapValues { (_, pubkeys) ->
                listOf(
                    Filter().kind(kind = Kind.fromEnum(KindEnum.ContactList))
                        .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                        .until(timestamp = timestamp)
                        .limit(pubkeys.size.toULong())
                )
            }
        val newestSubs = lazySubNewestWotPubkeys(until = timestamp)

        mergeRelayFilters(missingSubs, newestSubs).forEach { (relay, filters) ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private suspend fun lazySubNewestWotPubkeys(until: Timestamp): Map<RelayUrl, List<Filter>> {
        val newestCreatedAt = webOfTrustProvider.getNewestCreatedAt()?.toULong()
            ?: return emptyMap()
        if (newestCreatedAt >= until.asSecs()) return emptyMap()

        val friendPubkeys = friendProvider.getFriendPubkeys().map { PublicKey.fromHex(it) }
        val newWotFilter = listOf(
            Filter().kind(kind = Kind.fromEnum(KindEnum.ContactList))
                .authors(authors = friendPubkeys)
                .until(timestamp = until)
                .since(timestamp = Timestamp.fromSecs(newestCreatedAt + 1u))
                .limit(friendPubkeys.size.toULong())
        )
        return relayProvider.getReadRelays().associateWith { newWotFilter }
    }
}
