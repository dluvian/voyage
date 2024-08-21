package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.LAZY_RND_RESUB_LIMIT
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.MAX_KEYS
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.utils.limitRestricted
import com.dluvian.voyage.core.utils.mergeRelayFilters
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.event.LOCK_U64
import com.dluvian.voyage.data.model.CustomPubkeys
import com.dluvian.voyage.data.model.FriendPubkeysNoLock
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.ListPubkeys
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.PubkeySelection
import com.dluvian.voyage.data.model.SingularPubkey
import com.dluvian.voyage.data.model.WebOfTrustPubkeys
import com.dluvian.voyage.data.provider.FriendProvider
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.PubkeyProvider
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
    private val myPubkeyProvider: IMyPubkeyProvider,
    private val itemSetProvider: ItemSetProvider,
    private val pubkeyProvider: PubkeyProvider,
) {
    suspend fun lazySubMyAccountAndTrustData() {
        Log.d(TAG, "subMyAccountAndTrustData")
        lazySubMyAccount()
        delay(DELAY_1SEC)
        lazySubNip65s(selection = FriendPubkeysNoLock)
        delay(DELAY_1SEC)
        lazySubWebOfTrustPubkeys()
    }

    suspend fun lazySubMyAccount() {
        Log.d(TAG, "lazySubMyAccount")

        val hex = myPubkeyProvider.getPubkeyHex()

        val contactSince = friendProvider.getCreatedAt()?.toULong() ?: 1uL
        val topicSince = topicProvider.getCreatedAt()?.toULong() ?: 1uL
        val nip65Since = relayProvider.getCreatedAt(pubkey = hex)?.toULong() ?: 1uL
        val profileSince = room.profileDao().getMaxCreatedAt(pubkey = hex)?.toULong() ?: 1uL
        val isLocked = room.lockDao().isLocked(pubkey = hex)

        lazySubMyKind(
            kindAndSince = listOf(
                Pair(Kind.fromEnum(KindEnum.ContactList).asU16(), contactSince + 1uL),
                Pair(Kind.fromEnum(KindEnum.Interests).asU16(), topicSince + 1uL),
                Pair(Kind.fromEnum(KindEnum.RelayList).asU16(), nip65Since + 1uL),
                Pair(Kind.fromEnum(KindEnum.Metadata).asU16(), profileSince + 1uL),
            ).let {
                if (isLocked) it else it + Pair(LOCK_U64.toUShort(), 1uL)
            }
        )
    }

    suspend fun lazySubMyMainView() {
        Log.d(TAG, "lazySubMyMainView")
        val bookmarksSince = room.bookmarkDao().getMaxCreatedAt()?.toULong() ?: 1uL
        val muteSince = room.muteDao().getMaxCreatedAt()?.toULong() ?: 1uL
        lazySubMyKind(
            kindAndSince = listOf(
                Pair(Kind.fromEnum(KindEnum.Bookmarks).asU16(), bookmarksSince + 1uL),
                Pair(Kind.fromEnum(KindEnum.MuteList).asU16(), muteSince + 1uL)
            )
        )
    }

    suspend fun lazySubMyBookmarks() {
        Log.d(TAG, "lazySubMyBookmarks")
        val bookmarksSince = room.bookmarkDao().getMaxCreatedAt()?.toULong() ?: 1uL
        lazySubMyKind(
            kindAndSince = listOf(
                Pair(Kind.fromEnum(KindEnum.Bookmarks).asU16(), bookmarksSince + 1uL)
            )
        )
    }

    suspend fun lazySubMyMutes() {
        Log.d(TAG, "lazySubMyMutes")
        val muteSince = room.muteDao().getMaxCreatedAt()?.toULong() ?: 1uL
        lazySubMyKind(
            kindAndSince = listOf(
                Pair(Kind.fromEnum(KindEnum.MuteList).asU16(), muteSince + 1uL)
            )
        )
    }

    suspend fun lazySubMySets() {
        Log.d(TAG, "lazySubMySets")
        val timestamp = Timestamp.now()
        val pubkey = myPubkeyProvider.getPublicKey()

        val profileSetsSince = room.contentSetDao().getProfileSetMaxCreatedAt()?.toULong() ?: 1uL
        val myProfileSetsFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.FollowSet))
            .author(author = pubkey)
            .until(timestamp = timestamp)
            .since(timestamp = Timestamp.fromSecs(secs = profileSetsSince + 1u))
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)

        val topicSetsSince = room.contentSetDao().getTopicSetMaxCreatedAt()?.toULong() ?: 1uL
        val myTopicSetsFilter = Filter().kind(kind = Kind.fromEnum(KindEnum.InterestSet))
            .author(author = pubkey)
            .until(timestamp = timestamp)
            .since(timestamp = Timestamp.fromSecs(secs = topicSetsSince + 1u))
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
        val filters = listOf(myProfileSetsFilter, myTopicSetsFilter)

        relayProvider.getWriteRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubRepliesAndVotes(parentId: EventIdHex) {
        Log.d(TAG, "lazySubRepliesAndVotes for parent $parentId")
        val newestReplyTime = room.replyDao().getNewestReplyCreatedAt(parentId = parentId) ?: 1L
        val newestVoteTime = room.voteDao().getNewestVoteCreatedAt(postId = parentId) ?: 1L

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
            .since(timestamp = Timestamp.fromSecs((newestVoteTime + 1).toULong()))
            .until(timestamp = now)
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
        val filters = listOf(replyFilter, voteFilter)

        relayProvider.getReadRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubUnknownProfiles() {
        val pubkeys = mutableListOf<PubkeyHex>()
        pubkeys.addAll(friendProvider.getFriendsWithMissingProfile())
        pubkeys.addAll(webOfTrustProvider.getWotWithMissingProfile())

        lazySubUnknownProfiles(selection = CustomPubkeys(pubkeys = pubkeys))
    }

    suspend fun lazySubUnknownProfiles(selection: PubkeySelection) {
        val pubkeys = pubkeyProvider.getPubkeys(selection = selection).take(MAX_KEYS_SQL)
        val unknownPubkeys = pubkeys - room.profileDao()
            .filterKnownProfiles(pubkeys = pubkeys)
            .toSet()
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

    suspend fun lazySubNip65s(selection: PubkeySelection) {
        val missingPubkeys = when (selection) {
            FriendPubkeysNoLock -> friendProvider.getFriendsWithMissingNip65()

            is CustomPubkeys -> relayProvider.filterMissingPubkeys(
                pubkeys = selection.pubkeys.toList()
            )

            is SingularPubkey -> relayProvider.filterMissingPubkeys(pubkeys = selection.asList())

            is ListPubkeys -> itemSetProvider.getPubkeysWithMissingNip65(
                identifier = selection.identifier
            )

            Global -> emptyList()
            NoPubkeys -> emptyList()
            WebOfTrustPubkeys -> {
                Log.w(TAG, "We don't lazy sub nip65 of web of trust")
                emptyList()
            }
        }.toSet()

        Log.d(TAG, "Missing nip65s of ${missingPubkeys.size} pubkeys")
        // Don't return early. We need to call lazySubNewestNip65s later

        val now = Timestamp.now()
        val missingSubs = relayProvider
            .getObserveRelays(selection = CustomPubkeys(pubkeys = missingPubkeys))
            .mapValues { (_, pubkeys) ->
                val limitedPubkeys = pubkeys.takeRandom(MAX_KEYS)
                listOf(
                    Filter()
                        .kind(kind = Kind.fromEnum(KindEnum.RelayList))
                        .authors(authors = limitedPubkeys.map { PublicKey.fromHex(it) })
                        .until(timestamp = now)
                        .limitRestricted(limit = limitedPubkeys.size.toULong())
                )
            }
        val newestSubs = semiLazySubNewestNip65s(
            until = now,
            selection = selection,
            excludePubkeys = missingPubkeys
        )

        mergeRelayFilters(missingSubs, newestSubs).forEach { (relay, filters) ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private fun lazySubMyKind(kindAndSince: Collection<Pair<UShort, ULong>>) {
        if (kindAndSince.isEmpty()) return

        val filters = kindAndSince.map { (kind, since) ->
            Filter().kind(kind = Kind(kind = kind))
                .author(author = myPubkeyProvider.getPublicKey())
                .until(timestamp = Timestamp.now())
                .since(timestamp = Timestamp.fromSecs(secs = since))
                .limit(1u)
        }

        relayProvider.getWriteRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private suspend fun semiLazySubNewestNip65s(
        until: Timestamp,
        selection: PubkeySelection,
        excludePubkeys: Set<PubkeyHex> = emptySet()
    ): Map<RelayUrl, List<Filter>> {
        val newestCreatedAt = relayProvider.getNewestCreatedAt()?.toULong() ?: return emptyMap()
        if (newestCreatedAt >= until.asSecs()) return emptyMap()

        val pubkeys = pubkeyProvider.getPubkeys(selection = selection)
            .minus(excludePubkeys)
            .map { PublicKey.fromHex(it) }

        val newNip65Filter = listOf(
            Filter()
                .kind(kind = Kind.fromEnum(KindEnum.RelayList))
                .authors(authors = pubkeys)
                .until(timestamp = until)
                .since(timestamp = Timestamp.fromSecs(newestCreatedAt + 1u))
                .limitRestricted(limit = pubkeys.size.toULong())
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
            .getFriendPubkeysNoLock(max = MAX_KEYS)
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
