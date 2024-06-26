package com.dluvian.voyage.data.interactor

import android.util.Log
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.Topic
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
        pubkeys: List<PubkeyHex>
    ): Result<Event> {
        return nostrService.publishProfileSet(
            identifier = identifier,
            title = title,
            pubkeys = pubkeys.map { PublicKey.fromHex(it) },
            relayUrls = relayProvider.getPublishRelays()
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
        topics: List<Topic>
    ): Result<Event> {
        return nostrService.publishTopicSet(
            identifier = identifier,
            title = title,
            topics = topics,
            relayUrls = relayProvider.getPublishRelays()
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

    suspend fun addProfileToSet(pubkey: PubkeyHex, identifier: String): Result<Event> {
        val currentList = itemSetProvider.getPubkeysFromList(identifier = identifier)
        if (currentList.contains(pubkey)) {
            return Result.failure(IllegalStateException("Pubkey is already in list"))
        }

        return editProfileSet(
            identifier = identifier,
            title = itemSetProvider.getTitle(identifier = identifier),
            pubkeys = currentList + pubkey,
        )
    }
}
