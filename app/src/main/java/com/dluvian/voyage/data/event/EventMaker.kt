package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.EventIdHex
import com.dluvian.voyage.core.Label
import com.dluvian.voyage.core.OptionId
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.utils.createVoyageClientTag
import com.dluvian.voyage.data.account.AccountManager
import com.dluvian.voyage.data.nostr.Nip65Relay
import com.dluvian.voyage.data.nostr.RelayUrl
import com.dluvian.voyage.data.nostr.createDescriptionTag
import com.dluvian.voyage.data.nostr.createMentionTags
import com.dluvian.voyage.data.nostr.createQuoteTags
import com.dluvian.voyage.data.nostr.createSubjectTag
import com.dluvian.voyage.data.nostr.createTitleTag
import com.dluvian.voyage.data.preferences.EventPreferences
import rust.nostr.sdk.Bookmarks
import rust.nostr.sdk.Contact
import rust.nostr.sdk.Coordinate
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventBuilder
import rust.nostr.sdk.EventId
import rust.nostr.sdk.GitIssue
import rust.nostr.sdk.Interests
import rust.nostr.sdk.Keys
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindEnum
import rust.nostr.sdk.Metadata
import rust.nostr.sdk.MuteList
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.Tag

private const val TAG = "EventMaker"

class EventMaker(
    private val accountManager: AccountManager,
    private val eventPreferences: EventPreferences,
) {
    suspend fun buildPost(
        subject: String,
        content: String,
        topics: List<Topic>,
        mentions: List<PubkeyHex>,
        quotes: List<String>,
        isAnon: Boolean,
    ): Result<Event> {
        val tags = mutableListOf<Tag>()
        if (subject.isNotEmpty()) tags.add(createSubjectTag(subject = subject))
        topics.forEach { tags.add(Tag.hashtag(hashtag = it)) }
        if (mentions.isNotEmpty()) tags.addAll(createMentionTags(pubkeys = mentions))
        if (quotes.isNotEmpty()) tags.addAll(createQuoteTags(eventIdHexOrCoordinates = quotes))
        addClientTag(tags = tags, isAnon = isAnon)

        return signEvent(
            eventBuilder = EventBuilder.textNote(content = content).tags(tags = tags),
            isAnon = isAnon
        )
    }

    suspend fun buildPoll(
        question: String,
        options: List<Pair<OptionId, Label>>,
        endsAt: Long,
        pollRelays: List<RelayUrl>,
        topics: List<Topic>,
        mentions: List<PubkeyHex>,
        quotes: List<String>,
        isAnon: Boolean,
    ): Result<Event> {
        val tags = mutableListOf<Tag>()
        options.forEach { (id, label) -> tags.add(Tag.parse(listOf("option", id, label))) }
        tags.add(Tag.parse(listOf("endsAt", "$endsAt")))
        pollRelays.forEach { tags.add(Tag.parse(listOf("relay", it))) }
        topics.forEach { tags.add(Tag.hashtag(hashtag = it)) }
        tags.addAll(createMentionTags(pubkeys = mentions))
        tags.addAll(createQuoteTags(eventIdHexOrCoordinates = quotes))
        addClientTag(tags = tags, isAnon = isAnon)

        return signEvent(
            eventBuilder = EventBuilder(kind = Kind(kind = POLL_U16), content = question)
                .tags(tags = tags),
            isAnon = isAnon
        )
    }

    suspend fun buildReply(
        parent: Event,
        mentions: List<PubkeyHex>,
        quotes: List<String>,
        relayHint: RelayUrl?,
        content: String,
        isAnon: Boolean,
    ): Result<Event> {
        val tags = mutableListOf<Tag>()
        if (mentions.isNotEmpty()) tags.addAll(createMentionTags(pubkeys = mentions))
        if (quotes.isNotEmpty()) tags.addAll(createQuoteTags(eventIdHexOrCoordinates = quotes))
        addClientTag(tags = tags, isAnon = isAnon)

        return signEvent(
            eventBuilder = EventBuilder.textNoteReply(
                content = content,
                replyTo = parent,
                root = parent,
                relayUrl = relayHint,
            ).tags(tags = tags),
            isAnon = isAnon
        )
    }

    suspend fun buildComment(
        parent: Event,
        mentions: List<PubkeyHex>,
        quotes: List<String>,
        topics: List<Topic>,
        relayHint: RelayUrl?,
        content: String,
        isAnon: Boolean,
    ): Result<Event> {
        val tags = mutableListOf<Tag>()
        if (mentions.isNotEmpty()) tags.addAll(createMentionTags(pubkeys = mentions))
        if (quotes.isNotEmpty()) tags.addAll(createQuoteTags(eventIdHexOrCoordinates = quotes))
        if (topics.isNotEmpty()) tags.addAll(topics.map { Tag.hashtag(hashtag = it) })
        addClientTag(tags = tags, isAnon = isAnon)

        return signEvent(
            eventBuilder = EventBuilder.comment(
                content = content,
                commentTo = parent,
                relayUrl = relayHint,
            )
                .tags(tags = tags),
            isAnon = isAnon
        )
    }

    suspend fun buildCrossPost(
        crossPostedEvent: Event,
        topics: List<Topic>,
        relayHint: RelayUrl,
        isAnon: Boolean,
    ): Result<Event> {
        val tags = topics.map { Tag.hashtag(hashtag = it) }.toMutableList()
        addClientTag(tags = tags, isAnon = isAnon)

        return signEvent(
            eventBuilder = EventBuilder
                .repost(event = crossPostedEvent, relayUrl = relayHint)
                .tags(tags = tags),
            isAnon = isAnon
        )
    }

    suspend fun buildVote(eventId: EventId, content: String, mention: PublicKey): Result<Event> {
        val unsignedEvent = EventBuilder.reactionExtended(
            eventId = eventId,
            publicKey = mention,
            reaction = content,
        ).build(publicKey = accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildPollResponse(pollId: EventId, optionId: OptionId): Result<Event> {
        val unsignedEvent = EventBuilder(
            kind = Kind(kind = POLL_RESPONSE_U16),
            content = ""
        )
            .tags(
                tags = listOf(
                    Tag.event(pollId),
                    Tag.parse(listOf("response", optionId))
                )
            )
            .build(publicKey = accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildDelete(eventId: EventId): Result<Event> {
        val unsignedEvent = EventBuilder.delete(ids = listOf(eventId))
            .build(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildListDelete(identifier: String): Result<Event> {
        val pubkey = accountManager.getPublicKey()
        val coordinates = listOf(
            Coordinate(Kind.fromEnum(KindEnum.FollowSet), pubkey, identifier),
            Coordinate(Kind.fromEnum(KindEnum.InterestSet), pubkey, identifier)
        )
        val unsignedEvent = EventBuilder.delete(coordinates = coordinates).build(pubkey)

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildTopicList(topics: List<Topic>): Result<Event> {
        val interests = Interests(hashtags = topics)
        val unsignedEvent = EventBuilder.interests(list = interests)
            .build(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildBookmarkList(postIds: List<EventIdHex>): Result<Event> {
        val bookmarks = Bookmarks(eventIds = postIds.map { EventId.fromHex(it) })
        val unsignedEvent = EventBuilder.bookmarks(list = bookmarks)
            .build(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildMuteList(
        pubkeys: List<PubkeyHex>,
        topics: List<Topic>,
        words: List<String>
    ): Result<Event> {
        val mutes = MuteList(
            publicKeys = pubkeys.map { PublicKey.fromHex(it) },
            hashtags = topics,
            words = words,
        )
        val unsignedEvent = EventBuilder.muteList(list = mutes)
            .build(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildContactList(pubkeys: List<PubkeyHex>): Result<Event> {
        val contacts = pubkeys.map { Contact(pk = PublicKey.fromHex(it)) }
        val unsignedEvent = EventBuilder.contactList(list = contacts)
            .build(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildNip65(relays: List<Nip65Relay>): Result<Event> {
        val metadata = mutableMapOf<RelayUrl, RelayMetadata?>()

        relays.forEach {
            if (it.isRead && it.isWrite) metadata[it.url] = null
            else if (it.isRead) metadata[it.url] = RelayMetadata.READ
            else metadata[it.url] = RelayMetadata.WRITE
        }

        val unsignedEvent = EventBuilder.relayList(map = metadata)
            .build(accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildProfile(metadata: Metadata): Result<Event> {
        val unsignedEvent = EventBuilder.metadata(metadata)
            .build(publicKey = accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    suspend fun buildAuth(relayUrl: RelayUrl, challenge: String): Result<Event> {
        Log.d(TAG, "Build AUTH for $relayUrl")
        val unsignedEvent = runCatching {
            EventBuilder.auth(challenge = challenge, relayUrl = relayUrl)
                .build(publicKey = accountManager.getPublicKey())
        }

        return if (unsignedEvent.isSuccess) {
            accountManager.sign(unsignedEvent = unsignedEvent.getOrThrow())
        } else {
            Log.w(TAG, "EventBuilder.auth for $relayUrl failed")
            Result.failure(
                unsignedEvent.exceptionOrNull() ?: IllegalStateException("EventBuilder.auth failed")
            )
        }
    }

    suspend fun buildProfileSet(
        identifier: String,
        title: String,
        description: String,
        pubkeys: List<PublicKey>
    ): Result<Event> {
        return buildSet(
            title = title,
            description = description,
            eventBuilder = EventBuilder.followSet(identifier = identifier, publicKeys = pubkeys)
        )
    }

    suspend fun buildTopicSet(
        identifier: String,
        title: String,
        description: String,
        topics: List<Topic>
    ): Result<Event> {
        return buildSet(
            title = title,
            description = description,
            eventBuilder = EventBuilder.interestSet(identifier = identifier, hashtags = topics)
        )
    }

    suspend fun buildLock(): Result<Event> {
        // Enhancement request in rust-nostr once nip has been merged
        return signEvent(
            eventBuilder = EventBuilder(kind = Kind(LOCK_U16), content = ""),
            isAnon = false
        )
    }

    suspend fun buildGitIssue(
        repoCoordinate: Coordinate,
        subject: String,
        content: String,
        label: String,
        mentions: List<PubkeyHex>,
        quotes: List<String>,
        isAnon: Boolean,
    ): Result<Event> {
        val tags = mutableListOf<Tag>()
        if (quotes.isNotEmpty()) tags.addAll(createQuoteTags(eventIdHexOrCoordinates = quotes))

        val repoOwner = repoCoordinate.publicKey().toHex()
        val pubkeys = (mentions + repoOwner).distinct().map { PublicKey.fromHex(it) }

        val issue = GitIssue(
            content = content,
            repository = repoCoordinate,
            publicKeys = pubkeys,
            subject = subject,
            labels = listOf(label)
        )

        return signEvent(
            eventBuilder = EventBuilder.gitIssue(issue = issue).tags(tags = tags),
            isAnon = isAnon
        )
    }

    private suspend fun buildSet(
        title: String,
        description: String,
        eventBuilder: EventBuilder
    ): Result<Event> {
        val additionalTags = listOf(
            createTitleTag(title = title),
            createDescriptionTag(description = description)
        )
        val unsignedEvent = eventBuilder
            .tags(tags = additionalTags)
            .build(publicKey = accountManager.getPublicKey())

        return accountManager.sign(unsignedEvent = unsignedEvent)
    }

    private suspend fun signEvent(eventBuilder: EventBuilder, isAnon: Boolean): Result<Event> {
        return if (isAnon) {
            Result.success(eventBuilder.signWithKeys(Keys.generate()))
        } else {
            val unsignedEvent = eventBuilder.build(accountManager.getPublicKey())
            accountManager.sign(unsignedEvent = unsignedEvent)
        }
    }

    private fun addClientTag(tags: MutableList<Tag>, isAnon: Boolean) {
        if (isAnon || !eventPreferences.isAddingClientTag()) return

        tags.add(createVoyageClientTag())
    }
}
