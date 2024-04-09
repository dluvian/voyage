package com.dluvian.voyage.data.nostr

import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.RND_RESUB_COUNT
import com.dluvian.voyage.data.model.FilterWrapper
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import com.dluvian.voyage.data.room.dao.ProfileDao
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

class LazyNostrSubscriber(
    private val profileDao: ProfileDao,
    private val relayProvider: RelayProvider,
    private val subCreator: SubscriptionCreator,
    private val webOfTrustProvider: WebOfTrustProvider,
    private val friendProvider: FriendProvider,
) {
    suspend fun lazySubProfiles(pubkeys: Collection<PubkeyHex>) {
        if (pubkeys.isEmpty()) return
        val unknownPubkeys = pubkeys - profileDao.filterKnownProfiles(pubkeys = pubkeys).toSet()
        if (unknownPubkeys.isEmpty()) return

        val timestamp = Timestamp.now()

        relayProvider.getObserveRelays(pubkeys = unknownPubkeys).forEach { (relay, pubkeyBatch) ->
            val profileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
                .authors(authors = pubkeyBatch.map { PublicKey.fromHex(it) })
                .until(timestamp = timestamp)
            val filters = listOf(FilterWrapper(profileFilter))
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubWebOfTrustProfiles() {
        val pubkeys = webOfTrustProvider.getWotWithMissingProfiles()
        if (pubkeys.isEmpty()) return

        val timestamp = Timestamp.now()
        relayProvider
            .getObserveRelays(pubkeys = pubkeys)
            .forEach { (relay, pubkeys) ->
                val profileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
                    .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                val filters = listOf(FilterWrapper(profileFilter))
                subCreator.subscribe(relayUrl = relay, filters = filters)
            }
    }

    suspend fun semiLazySubFriendsNip65() {
        val friendsWithMissingNip65 = friendProvider.getFriendsWithMissingNip65()
        val randomResub = friendProvider.getFriendPubkeys(max = RND_RESUB_COUNT)
        val nip65Resub = (friendsWithMissingNip65 + randomResub).distinct()
        if (nip65Resub.isEmpty()) return

        val timestamp = Timestamp.now()
        relayProvider
            .getObserveRelays(pubkeys = nip65Resub)
            .forEach { (relay, pubkeys) ->
                val nip65Filter = Filter().kind(kind = Kind.fromEnum(KindEnum.RelayList))
                    .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                    .limit(pubkeys.size.toULong())
                val filters = listOf(FilterWrapper(nip65Filter))
                subCreator.subscribe(relayUrl = relay, filters = filters)
            }
    }

    suspend fun semiLazySubWebOfTrustPubkeys() {
        val friendsWithMissingContactList = friendProvider.getFriendsWithMissingContactList()
        val randomResub = friendProvider.getFriendPubkeys(max = RND_RESUB_COUNT)
        val webOfTrustResub = (friendsWithMissingContactList + randomResub).distinct()
        if (webOfTrustResub.isEmpty()) return

        val timestamp = Timestamp.now()
        relayProvider
            .getObserveRelays(pubkeys = webOfTrustResub)
            .forEach { (relay, pubkeys) ->
                val webOfTrustFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.ContactList))
                    .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                    .limit(pubkeys.size.toULong())
                val filters = listOf(FilterWrapper(webOfTrustFilter))
                subCreator.subscribe(relayUrl = relay, filters = filters)
            }
    }
}
