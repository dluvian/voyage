package com.dluvian.voyage.data.interactor

import android.util.Log
import com.dluvian.voyage.core.MAX_KEYS_SQL
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.model.ItemSetItem
import com.dluvian.voyage.core.model.ItemSetProfile
import com.dluvian.voyage.core.model.ItemSetTopic
import com.dluvian.voyage.data.event.EventValidator
import com.dluvian.voyage.data.nostr.NostrService
import com.dluvian.voyage.data.provider.ItemSetProvider
import com.dluvian.voyage.data.provider.RelayProvider
import com.dluvian.voyage.data.room.dao.tx.ProfileSetUpsertDao
import com.dluvian.voyage.data.room.dao.tx.TopicSetUpsertDao
import rust.nostr.protocol.Event
import rust.nostr.protocol.PublicKey

private const val TAG = "ItemSetEditor"

class ItemSetEditor(
    private val nostrService: NostrService,
    private val relayProvider: RelayProvider,
    private val profileSetUpsertDao: ProfileSetUpsertDao,
    private val topicSetUpsertDao: TopicSetUpsertDao,
    private val itemSetProvider: ItemSetProvider,
) {
    suspend fun editProfileSet(
        identifier: String,
        title: String,
        description: String,
        pubkeys: List<PubkeyHex>
    ): Result<Event> {
        return nostrService.publishProfileSet(
            identifier = identifier,
            title = title,
            description = description,
            pubkeys = pubkeys.map { PublicKey.fromHex(it) },
            relayUrls = relayProvider.getPublishRelays(addConnected = false) // TODO: write relays only
        ).onFailure {
            Log.w(TAG, "Failed to sign profile set", it)
        }.onSuccess {
            val validated = EventValidator.createValidatedProfileSet(event = it)
            if (validated == null) {
                val err = "Serialized topic profile event differs from input"
                Log.w(TAG, err)
                return Result.failure(IllegalStateException(err))
            }
            profileSetUpsertDao.upsertSet(set = validated)
        }
    }

    suspend fun editTopicSet(
        identifier: String,
        title: String,
        description: String,
        topics: List<Topic>
    ): Result<Event> {
        return nostrService.publishTopicSet(
            identifier = identifier,
            title = title,
            description = description,
            topics = topics,
            relayUrls = relayProvider.getPublishRelays(addConnected = false) // TODO: write relays only
        ).onFailure {
            Log.w(TAG, "Failed to sign topic set", it)
        }.onSuccess {
            val validated = EventValidator.createValidatedTopicSet(event = it)
            if (validated == null) {
                val err = "Serialized topic set event differs from input"
                Log.w(TAG, err)
                return Result.failure(IllegalStateException(err))
            }
            topicSetUpsertDao.upsertSet(set = validated)
        }
    }

    suspend fun addItemToSet(item: ItemSetItem, identifier: String): Result<Event> {
        val currentList = when (item) {
            is ItemSetProfile -> itemSetProvider.getPubkeysFromList(identifier = identifier)
            is ItemSetTopic -> itemSetProvider.getTopicsFromList(identifier = identifier)
        }

        if (currentList.contains(item.value)) {
            return Result.failure(IllegalStateException("Item is already in list"))
        }
        if (currentList.size >= MAX_KEYS_SQL) {
            return Result.failure(IllegalArgumentException("List is already full"))
        }

        val titleAndDescription = itemSetProvider.getTitleAndDescription(identifier = identifier)

        return when (item) {
            is ItemSetProfile -> {
                editProfileSet(
                    identifier = identifier,
                    title = titleAndDescription.title,
                    description = titleAndDescription.description,
                    pubkeys = currentList + item.pubkey,
                )
            }

            is ItemSetTopic -> {
                editTopicSet(
                    identifier = identifier,
                    title = titleAndDescription.title,
                    description = titleAndDescription.description,
                    topics = currentList + item.topic,
                )
            }
        }
    }
}
