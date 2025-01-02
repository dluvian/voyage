package com.dluvian.voyage.data.nostr

import android.util.Log
import com.dluvian.voyage.core.DELAY_1SEC
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.LAZY_RND_RESUB_LIMIT
import com.dluvian.voyage.core.MAX_KEYS
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.utils.mergeRelayFilters
import com.dluvian.voyage.core.utils.takeRandom
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.event.LOCK_U16
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
import com.dluvian.voyage.data.room.entity.main.poll.PollEntity
import kotlinx.coroutines.delay
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindEnum
import rust.nostr.sdk.Nip19Profile
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.Timestamp
import kotlin.random.Random

private const val TAG = "LazyNostrSubscriber"

class LazyNostrSubscriber(
    val subCreator: SubscriptionCreator,
    private val room: AppDatabase,
    private val relayProvider: RelayProvider,
    private val filterCreator: FilterCreator,
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
        lazySubWebOfTrust()
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
                if (isLocked) it else it + Pair(LOCK_U16, 1uL)
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

    suspend fun semiLazySubProfile(nprofile: Nip19Profile) {
        val profileFilter = filterCreator.getSemiLazyProfileFilter(pubkey = nprofile.publicKey())
        val filters = listOf(profileFilter)

        relayProvider.getObserveRelays(nprofile = nprofile).forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    fun lazySubWotLocks(prioritizeFriends: Boolean) {
        getWotLockFilters(prioritizeFriends = prioritizeFriends).forEach { (relay, filters) ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubOpenProfile(nprofile: Nip19Profile, subMeta: Boolean) {
        val lockFilter = filterCreator.getLazyLockFilter(pubkey = nprofile.publicKey())
        val nip65Filter = filterCreator.getLazyNip65Filter(pubkey = nprofile.publicKey())
        val profileFilter = if (subMeta) {
            filterCreator.getSemiLazyProfileFilter(pubkey = nprofile.publicKey())
        } else null

        val filters = listOf(nip65Filter, lockFilter, profileFilter).mapNotNull { it }

        relayProvider.getObserveRelays(nprofile = nprofile, includeConnected = true)
            .forEach { relay -> subCreator.subscribe(relayUrl = relay, filters = filters) }
    }

    suspend fun lazySubMySets() {
        Log.d(TAG, "lazySubMySets")

        val filters = listOf(
            filterCreator.getLazyMyProfileSetsFilter(),
            filterCreator.getLazyMyTopicSetsFilter()
        )

        relayProvider.getWriteRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubRepliesAndVotes(parentId: EventIdHex) {
        Log.d(TAG, "lazySubRepliesAndVotes for parent $parentId")

        val id = EventId.parse(parentId)
        val filters = listOf(
            filterCreator.getLazyReplyFilter(parentId = id),
            filterCreator.getLazyVoteFilter(parentId = id)
        )

        relayProvider.getReadRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubPollResponses(poll: PollEntity) {
        val filters = listOf(filterCreator.getLazyPollResponseFilter(poll = poll))

        relayProvider.getReadRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    suspend fun lazySubUnknownProfiles() {
        val pubkeys = mutableListOf<PubkeyHex>()
        pubkeys.addAll(friendProvider.getFriendsWithMissingProfile())
        pubkeys.addAll(webOfTrustProvider.getWotWithMissingProfile().minus(pubkeys.toSet()))

        lazySubUnknownProfiles(selection = CustomPubkeys(pubkeys = pubkeys), checkDb = false)
    }

    suspend fun lazySubUnknownProfiles(selection: PubkeySelection, checkDb: Boolean = true) {
        val pubkeys = pubkeyProvider.getPubkeys(selection = selection).take(MAX_KEYS_SQL)
        val unknownPubkeys = if (checkDb) pubkeys - room.profileDao()
            .filterKnownProfiles(pubkeys = pubkeys)
            .toSet() else pubkeys
        if (unknownPubkeys.isEmpty()) return

        val now = Timestamp.now()

        relayProvider.getObserveRelays(selection = CustomPubkeys(pubkeys = unknownPubkeys))
            .forEach { (relay, pubkeyBatch) ->
                if (pubkeyBatch.isNotEmpty()) {
                    val filter = filterCreator.getProfileFilter(
                        pubkeys = pubkeyBatch.takeRandom(MAX_KEYS).map { PublicKey.parse(it) },
                        until = now
                    )
                    val filters = listOf(filter)
                    subCreator.subscribe(relayUrl = relay, filters = filters)
                }
            }
    }

    suspend fun lazySubNip65(nprofile: Nip19Profile) {
        val filters = listOf(
            filterCreator.getLazyNip65Filter(pubkey = nprofile.publicKey())
        )
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
                listOf(
                    filterCreator.getNip65Filter(
                        pubkeys = pubkeys.takeRandom(MAX_KEYS).map { PublicKey.parse(it) },
                        until = now
                    )
                )
            }
        val newestSubs = getNewestNip65sFilters(
            selection = selection,
            excludePubkeys = missingPubkeys
        )

        mergeRelayFilters(missingSubs, newestSubs).forEach { (relay, filters) ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private fun lazySubMyKind(kindAndSince: Collection<Pair<UShort, ULong>>) {
        if (kindAndSince.isEmpty()) return

        val filters = filterCreator.getMyKindFilter(kindAndSince = kindAndSince)

        relayProvider.getWriteRelays().forEach { relay ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private suspend fun getNewestNip65sFilters(
        selection: PubkeySelection,
        excludePubkeys: Set<PubkeyHex> = emptySet()
    ): Map<RelayUrl, List<Filter>> {
        val pubkeys = pubkeyProvider.getPubkeys(selection = selection)
            .minus(excludePubkeys)
            .map { PublicKey.parse(it) }
        val filters = listOf(
            filterCreator.getLazyNewestNip65Filter(pubkeys = pubkeys) ?: return emptyMap()
        )

        return relayProvider.getReadRelays(includeConnected = true).associateWith { filters }
    }

    private suspend fun lazySubWebOfTrust() {
        val friendsWithMissingContacts = friendProvider.getFriendsWithMissingContactList()
        Log.d(TAG, "Missing contact lists of ${friendsWithMissingContacts.size} pubkeys")
        // Don't return early. We need to call lazySubNewestWotPubkeys later

        val now = Timestamp.now()
        val missingSubs = relayProvider
            .getObserveRelays(selection = CustomPubkeys(pubkeys = friendsWithMissingContacts))
            .mapValues { (_, pubkeys) ->
                val filter = filterCreator.getContactFilter(
                    pubkeys = pubkeys.takeRandom(MAX_KEYS).map { PublicKey.parse(it) },
                    until = now,
                )
                listOf(filter)
            }

        mergeRelayFilters(
            missingSubs,
            getNewestWotPubkeysFilters(until = now),
            getWotLockFilters()
        ).forEach { (relay, filters) ->
            subCreator.subscribe(relayUrl = relay, filters = filters)
        }
    }

    private fun getWotLockFilters(
        prioritizeFriends: Boolean = Random.nextBoolean() // Prioritize friends half of the time
    ): Map<RelayUrl, List<Filter>> {
        val pubkeys = webOfTrustProvider.getFriendsAndWebOfTrustPubkeys(
            max = MAX_KEYS,
            friendsFirst = prioritizeFriends,
        ).map { PublicKey.parse(it) }

        if (pubkeys.isEmpty()) return emptyMap()

        val lockFilter = filterCreator.getLockFilter(pubkeys = pubkeys) ?: return emptyMap()
        val filters = listOf(lockFilter)

        return relayProvider.getReadRelays().associateWith { filters }
    }

    private suspend fun getNewestWotPubkeysFilters(until: Timestamp): Map<RelayUrl, List<Filter>> {
        val newestCreatedAt = webOfTrustProvider.getNewestCreatedAt()?.toULong()
            ?: return emptyMap()
        if (newestCreatedAt >= until.asSecs()) return emptyMap()

        val friendPubkeys = friendProvider
            .getFriendPubkeysNoLock(max = MAX_KEYS)
            .map { PublicKey.parse(it) }

        if (friendPubkeys.isEmpty()) return emptyMap()

        val newWotFilter = listOf(
            filterCreator.getContactFilter(
                pubkeys = friendPubkeys,
                since = Timestamp.fromSecs(newestCreatedAt + 1u),
                until = until,
                limit = LAZY_RND_RESUB_LIMIT
            )
        )
        return relayProvider.getReadRelays().associateWith { newWotFilter }
    }
}
