package com.dluvian.voyage.data.nostr

import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.MAX_EVENTS_TO_SUB
import com.dluvian.voyage.core.utils.limitRestricted
import com.dluvian.voyage.core.utils.replyKinds
import com.dluvian.voyage.core.utils.threadableKinds
import com.dluvian.voyage.data.account.IMyPubkeyProvider
import com.dluvian.voyage.data.event.LOCK_U16
import com.dluvian.voyage.data.event.POLL_RESPONSE_U16
import com.dluvian.voyage.data.provider.LockProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.AppDatabase
import com.dluvian.voyage.data.room.entity.main.poll.PollEntity
import rust.nostr.sdk.EventId
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindEnum
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.Timestamp

class FilterCreator(
    private val room: AppDatabase,
    private val myPubkeyProvider: IMyPubkeyProvider,
    private val lockProvider: LockProvider,
    private val relayProvider: RelayProvider,
) {
    companion object {
        fun createReactionaryFilter(
            ids: List<EventId>,
            kinds: List<Kind>,
            until: Timestamp,
        ): Filter {
            return Filter()
                .kinds(kinds = kinds)
                .events(ids = ids)
                .until(timestamp = until)
                .limitRestricted(limit = MAX_EVENTS_TO_SUB)
        }
    }

    fun getMyKindFilter(kindAndSince: Collection<Pair<UShort, ULong>>): List<Filter> {
        if (kindAndSince.isEmpty()) return emptyList()

        val myPubkey = myPubkeyProvider.getPublicKey()
        val now = Timestamp.now()

        return kindAndSince.map { (kind, since) ->
            Filter().kind(kind = Kind(kind = kind))
                .author(author = myPubkey)
                .until(timestamp = now)
                .since(timestamp = Timestamp.fromSecs(secs = since))
                .limit(1u)
        }
    }

    suspend fun getSemiLazyProfileFilter(pubkey: PublicKey): Filter {
        val profileSince = room.profileDao()
            .getMaxCreatedAt(pubkey = pubkey.toHex())
            ?.toULong()
            ?: 1uL

        return getProfileFilter(
            pubkeys = listOf(pubkey),
            since = Timestamp.fromSecs(profileSince), // No +1 because we don't cache all fields
        )
    }

    fun getProfileFilter(
        pubkeys: List<PublicKey>,
        since: Timestamp = Timestamp.fromSecs(1uL),
        until: Timestamp = Timestamp.now(),
    ): Filter {
        return Filter()
            .kind(kind = Kind.fromEnum(KindEnum.Metadata))
            .authors(authors = pubkeys)
            .since(timestamp = since)
            .until(timestamp = until)
            .limitRestricted(limit = pubkeys.size.toULong())
    }

    fun getLazyLockFilter(pubkey: PublicKey): Filter? {
        if (lockProvider.isLocked(pubkey = pubkey.toHex())) return null

        return getLockFilter(pubkeys = listOf(pubkey))
    }

    fun getLockFilter(pubkeys: List<PublicKey>): Filter? {
        if (pubkeys.isEmpty()) return null

        return Filter()
            .kind(kind = Kind(LOCK_U16))
            .authors(authors = pubkeys)
            .until(timestamp = Timestamp.now())
            .limitRestricted(limit = pubkeys.size.toULong())
    }

    suspend fun getLazyNewestNip65Filter(pubkeys: List<PublicKey>): Filter? {
        if (pubkeys.isEmpty()) return null
        val newestCreatedAt = relayProvider.getNewestCreatedAt()?.toULong() ?: return null
        val now = Timestamp.now()
        if (newestCreatedAt >= now.asSecs()) return null

        return getNip65Filter(
            pubkeys = pubkeys,
            since = Timestamp.fromSecs((newestCreatedAt + 1u)),
            until = now,
        )
    }

    suspend fun getLazyNip65Filter(pubkey: PublicKey): Filter {
        val nip65Since = relayProvider.getCreatedAt(pubkey = pubkey.toHex()) ?: 1

        return getNip65Filter(
            pubkeys = listOf(pubkey),
            since = Timestamp.fromSecs((nip65Since + 1).toULong())
        )
    }

    fun getNip65Filter(
        pubkeys: List<PublicKey>,
        until: Timestamp = Timestamp.now(),
        since: Timestamp = Timestamp.fromSecs(1uL)
    ): Filter {
        return Filter()
            .kind(kind = Kind.fromEnum(KindEnum.RelayList))
            .authors(authors = pubkeys)
            .since(timestamp = since)
            .until(timestamp = until)
            .limitRestricted(limit = pubkeys.size.toULong())
    }

    suspend fun getLazyMyProfileSetsFilter(): Filter {
        val profileSetsSince = room.contentSetDao().getProfileSetMaxCreatedAt()?.toULong() ?: 1uL

        return Filter().kind(kind = Kind.fromEnum(KindEnum.FollowSet))
            .author(author = myPubkeyProvider.getPublicKey())
            .until(timestamp = Timestamp.now())
            .since(timestamp = Timestamp.fromSecs(secs = profileSetsSince + 1u))
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
    }

    suspend fun getLazyMyTopicSetsFilter(): Filter {
        val topicSetsSince = room.contentSetDao().getTopicSetMaxCreatedAt()?.toULong() ?: 1uL

        return Filter().kind(kind = Kind.fromEnum(KindEnum.InterestSet))
            .author(author = myPubkeyProvider.getPublicKey())
            .until(timestamp = Timestamp.now())
            .since(timestamp = Timestamp.fromSecs(secs = topicSetsSince + 1u))
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
    }

    suspend fun getLazyReplyFilter(parentId: EventId): Filter {
        val newestReplyTime = room.someReplyDao()
            .getNewestReplyCreatedAt(parentId = parentId.toHex()) ?: 1L

        return Filter()
            .kinds(kinds = replyKinds)
            .events(ids = listOf(parentId))
            .since(timestamp = Timestamp.fromSecs((newestReplyTime + 1).toULong()))
            .until(timestamp = Timestamp.now())
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
    }

    suspend fun getLazyPollResponseFilter(poll: PollEntity): Filter {
        val endsAt = poll.endsAt ?: getCurrentSecs()
        val newestResponseTime = room.pollResponseDao()
            .getLatestResponseTime(pollId = poll.eventId) ?: 1L

        return Filter()
            .kind(kind = Kind(POLL_RESPONSE_U16))
            .event(eventId = EventId.fromHex(poll.eventId))
            .since(timestamp = Timestamp.fromSecs((newestResponseTime + 1).toULong()))
            .until(timestamp = Timestamp.fromSecs(endsAt.toULong()))
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
    }

    suspend fun getLazyVoteFilter(parentId: EventId): Filter {
        val newestVoteTime = room.voteDao().getNewestVoteCreatedAt(postId = parentId.toHex()) ?: 1L

        return Filter()
            .kind(kind = Kind.fromEnum(KindEnum.Reaction))
            .events(ids = listOf(parentId))
            .since(timestamp = Timestamp.fromSecs((newestVoteTime + 1).toULong()))
            .until(timestamp = Timestamp.now())
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
    }

    fun getContactFilter(
        pubkeys: List<PublicKey>,
        since: Timestamp = Timestamp.fromSecs(1u),
        until: Timestamp = Timestamp.now(),
        limit: ULong = pubkeys.size.toULong(),
    ): Filter {
        return Filter()
            .kind(kind = Kind.fromEnum(KindEnum.ContactList))
            .authors(authors = pubkeys)
            .since(timestamp = since)
            .until(timestamp = until)
            .limitRestricted(limit = limit)
    }

    fun getPostFilter(eventId: EventId): Filter {
        return Filter()
            .kinds(kinds = threadableKinds)
            .id(id = eventId)
            .until(timestamp = Timestamp.now())
            .limit(limit = 1u)
    }

    fun getPollResponseFilter(
        pollIds: List<EventIdHex>,
        since: Timestamp,
        until: Timestamp
    ): Filter {
        return Filter()
            .kind(kind = Kind(kind = POLL_RESPONSE_U16))
            .events(ids = pollIds.map { EventId.fromHex(it) })
            .since(timestamp = since)
            .until(timestamp = until)
            .limitRestricted(limit = MAX_EVENTS_TO_SUB)
    }
}
