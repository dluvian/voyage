package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.nostr_kt.SubId
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

private const val TAG = "LazyNostrSubscriber"

class LazyNostrSubscriber(
    private val profileDao: ProfileDao,
    private val relayProvider: RelayProvider,
    private val nostrClient: NostrClient,
    private val syncedFilterCache: MutableMap<SubId, List<FilterWrapper>>,
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
            subscribe(relayUrl = relay, filters = filters)
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
                subscribe(relayUrl = relay, filters = filters)
            }
    }

    suspend fun semiLazySubFriendsNip65() {
        val friendsWithMissingNip65 = friendProvider.getFriendsWithMissingNip65()
        val randomResub = friendProvider.getFriendPubkeys(limited = true, max = RND_RESUB_COUNT)
        val nip65Resub = (friendsWithMissingNip65 + randomResub).distinct()
        if (nip65Resub.isEmpty()) return

        val timestamp = Timestamp.now()
        relayProvider
            .getObserveRelays(pubkeys = nip65Resub)
            .forEach { (relay, pubkeys) ->
                val nip65Filter = Filter().kind(kind = Kind.fromEnum(KindEnum.RelayList))
                    .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                val filters = listOf(FilterWrapper(nip65Filter))
                subscribe(relayUrl = relay, filters = filters)
            }
    }

    suspend fun semiLazySubWebOfTrustPubkeys() {
        val friendsWithMissingContactList = friendProvider.getFriendsWithMissingContactList()
        val randomResub = friendProvider.getFriendPubkeys(limited = true, max = RND_RESUB_COUNT)
        val webOfTrustResub = (friendsWithMissingContactList + randomResub).distinct()
        if (webOfTrustResub.isEmpty()) return

        val timestamp = Timestamp.now()
        relayProvider
            .getObserveRelays(pubkeys = webOfTrustResub)
            .forEach { (relay, pubkeys) ->
                val webOfTrustFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.ContactList))
                    .authors(authors = pubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                val filters = listOf(FilterWrapper(webOfTrustFilter))
                subscribe(relayUrl = relay, filters = filters)
            }
    }

    fun subscribe(relayUrl: RelayUrl, filters: List<FilterWrapper>): SubId? {
        if (filters.isEmpty()) return null
        Log.d(TAG, "Subscribe ${filters.size} in $relayUrl")

        val subId = nostrClient.subscribe(filters = filters.map { it.filter }, relayUrl = relayUrl)
        if (subId == null) {
            Log.w(TAG, "Failed to create subscription ID")
            return null
        }
        syncedFilterCache[subId] = filters

        return subId
    }
}
