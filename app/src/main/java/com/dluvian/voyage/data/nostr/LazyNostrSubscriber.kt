package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.nostr_kt.RelayUrl
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.LAZY_RND_RESUB_LIMIT
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.MAX_KEYS
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.limitRestricted
import com.dluvian.voyage.core.mergeRelayFilters
import com.dluvian.voyage.core.takeRandom
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.model.CustomPubkeys
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.provider.TopicProvider
import com.dluvian.voyage.data.provider.WebOfTrustProvider
import com.dluvian.voyage.data.room.AppDatabase
import kotlinx.coroutines.delay
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Filter
import rust.nostr.protocol.Kind
import rust.nostr.protocol.KindEnum
import rust.nostr.protocol.Nip19Profile
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.Timestamp

private const val TAG = "LazyNostrSubscriber"

class LazyNostrSubscriber(
    private val room: AppDatabase,
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

    suspend fun lazySubRepliesAndVotes(parentId: EventIdHex) {
        Log.d(TAG, "lazySubRepliesAndVotes for parent $parentId")
        val newestReplyTime = room.replyDao().getNewestReplyCreatedAt(parentId = parentId) ?: 0L
        val newestVoteTime = room.voteDao().getNewestVoteCreatedAt(postId = parentId) ?: 0L
        val votePubkeys = webOfTrustProvider
            .getFriendsAndWebOfTrustPubkeys(includeMyself = true, max = MAX_KEYS)

        val now = Timestamp.now()
        val ids = listOf(EventId.fromHex(hex = parentId))
        val replyFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.TextNote))
            .events(ids = ids)
            .since(timestamp = Timestamp.fromSecs((newestReplyTime + 1).toULong()))
            .until(timestamp = now)
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
        val voteFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.Reaction))
            .events(ids = ids)
            .authors(authors = votePubkeys.map { PublicKey.fromHex(hex = it) })
            .since(timestamp = Timestamp.fromSecs((newestVoteTime + 1).toULong()))
            .until(timestamp = now)
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
        val filters = listOf(replyFilter, voteFilter)

        relayProvider.getReadRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubUnknownProfiles(pubkeys: Collection<PubkeyHex>) {
        if (pubkeys.isEmpty()) return
        val unknownPubkeys =
            pubkeys - room.profileDao().filterKnownProfiles(pubkeys = pubkeys).toSet()
        if (unknownPubkeys.isEmpty()) return

        val timestamp = Timestamp.now()

        relayProvider.getObserveRelays(selection = CustomPubkeys(pubkeys = unknownPubkeys))
            .forEach { (relay, pubkeyBatch) ->
                val limitedPubkeys = pubkeyBatch.takeRandom(MAX_KEYS)
                val profileFilter = Filter()
                    .kind(kind = Kind.fromEnum(KindEnum.Metadata))
                    .authors(authors = limitedPubkeys.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                    .limitRestricted(limit = limitedPubkeys.size.toULong())
                val filters = listOf(profileFilter)
                subCreator.subscribe(relayUrl = relay, filters = filters)
            }
    }

    suspend fun lazySubUnknownProfiles() {
        val pubkeys = mutableListOf<PubkeyHex>()
        pubkeys.addAll(friendProvider.getFriendsWithMissingProfile())
        pubkeys.addAll(webOfTrustProvider.getWotWithMissingProfile())
        val toSub = pubkeys.distinct()
        Log.d(TAG, "Subscribe to ${toSub.size} unknown profiles")
        if (toSub.isEmpty()) return

        val timestamp = Timestamp.now()

        relayProvider.getObserveRelays(selection = CustomPubkeys(pubkeys = toSub))
            .forEach { (relay, pubkeyBatch) ->
                val limitedBatch = pubkeyBatch.takeRandom(MAX_KEYS)
                val profileFilter = Filter()
                    .kind(kind = Kind.fromEnum(KindEnum.Metadata))
                    .authors(authors = limitedBatch.map { PublicKey.fromHex(it) })
                    .until(timestamp = timestamp)
                    .limitRestricted(limit = limitedBatch.size.toULong())
                val filters = listOf(profileFilter)
                subCreator.subscribe(relayUrl = relay, filters = filters)
            }
    }

    suspend fun lazySubNip65(nprofile: Nip19Profile) {
        val since = relayProvider.getCreatedAt(pubkey = nprofile.publicKey().toHex()) ?: 1
        val nip65Filter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.RelayList))
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
        val myContactFilter = Filter()
            .kind(kind = Kind.fromEnum(KindEnum.ContactList))
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

        val profileSince = room.profileDao().getMaxCreatedAt(pubkey = hex)?.toULong() ?: 1uL
        val myProfileFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Metadata))
            .author(author = pubkey)
            .until(timestamp = timestamp)
            .since(timestamp = Timestamp.fromSecs(secs = profileSince + 1u))
            .limit(1u)

        val bookmarksSince = room.bookmarkDao().getMaxCreatedAt()?.toULong() ?: 1uL
        val myBookmarksFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.Bookmarks))
            .author(author = pubkey)
            .until(timestamp = timestamp)
            .since(timestamp = Timestamp.fromSecs(secs = bookmarksSince + 1u))
            .limit(1u)

        val filters = listOf(
            myContactFilter,
            myTopicsFilter,
            myNip65Filter,
            myProfileFilter,
            myBookmarksFilter
        )

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
            .getObserveRelays(selection = CustomPubkeys(pubkeys = missingPubkeys))
            .mapValues { (_, pubkeys) ->
                val limitedPubkeys = pubkeys.takeRandom(MAX_KEYS)
                listOf(
                    Filter()
                        .kind(kind = Kind.fromEnum(KindEnum.RelayList))
                        .authors(authors = limitedPubkeys.map { PublicKey.fromHex(it) })
                        .until(timestamp = timestamp)
                        .limitRestricted(limit = limitedPubkeys.size.toULong())
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

        val friendPubkeys = friendProvider
            .getFriendPubkeys(max = MAX_KEYS)
            .map { PublicKey.fromHex(it) }

        val newNip65Filter = listOf(
            Filter()
                .kind(kind = Kind.fromEnum(KindEnum.RelayList))
                .authors(authors = friendPubkeys)
                .until(timestamp = until)
                .since(timestamp = Timestamp.fromSecs(newestCreatedAt + 1u))
                .limitRestricted(limit = friendPubkeys.size.toULong())
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
            .getObserveRelays(selection = CustomPubkeys(pubkeys = friendsWithMissingContacts))
            .mapValues { (_, pubkeys) ->
                val limitedPubkeys = pubkeys.takeRandom(MAX_KEYS)
                listOf(
                    Filter()
                        .kind(kind = Kind.fromEnum(KindEnum.ContactList))
                        .authors(authors = limitedPubkeys.map { PublicKey.fromHex(it) })
                        .until(timestamp = timestamp)
                        .limitRestricted(limit = limitedPubkeys.size.toULong())
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

        val friendPubkeys = friendProvider
            .getFriendPubkeys(max = MAX_KEYS)
            .map { PublicKey.fromHex(it) }
        val newWotFilter = listOf(
            Filter()
                .kind(kind = Kind.fromEnum(KindEnum.ContactList))
                .authors(authors = friendPubkeys)
                .until(timestamp = until)
                .since(timestamp = Timestamp.fromSecs(newestCreatedAt + 1u))
                .limitRestricted(limit = LAZY_RND_RESUB_LIMIT)
        )
        return relayProvider.getReadRelays().associateWith { newWotFilter }
    }
}
