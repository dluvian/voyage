package com.dluvian.voyage.data.provider

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.createAdvancedProfile
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.model.ItemSetMeta
import com.dluvian.voyage.data.room.AppDatabase
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ItemSetProvider(
    private val room: AppDatabase,
    private val pubkeyProvider: IPubkeyProvider,
    private val friendProvider: FriendProvider,
    private val muteProvider: MuteProvider,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val allPubkeys = room.itemSetDao().getAllPubkeysFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    val identifier = mutableStateOf("")
    val title = mutableStateOf("")
    val profiles = mutableStateOf(emptyList<AdvancedProfileView>())
    val topics = mutableStateOf(emptyList<Topic>())

    suspend fun loadList(identifier: String) {
        this.identifier.value = identifier

        if (identifier != this.identifier.value) {
            title.value = ""
            profiles.value = emptyList()
            topics.value = emptyList()
        }

        title.value = getTitle(identifier = identifier)
        profiles.value = getProfilesFromList(identifier = identifier)
        topics.value = getTopicsFromList(identifier = identifier)
    }

    fun getMySetsFlow(): Flow<List<ItemSetMeta>> {
        return combine(
            room.itemSetDao().getMyProfileSetMetasFlow().firstThenDistinctDebounce(SHORT_DEBOUNCE),
            room.itemSetDao().getMyTopicSetMetasFlow().firstThenDistinctDebounce(SHORT_DEBOUNCE)
        ) { profileSets, topicSets ->
            profileSets.plus(topicSets).distinctBy { it.identifier }.sortedBy { it.title }
        }
    }

    suspend fun getAddableSets(pubkey: PubkeyHex): List<ItemSetMeta> {
        return room.itemSetDao().getAddableSets(pubkey = pubkey).sortedBy { it.title }
    }

    suspend fun getNonAddableSets(pubkey: PubkeyHex): List<ItemSetMeta> {
        return room.itemSetDao().getNonAddableSets(pubkey = pubkey).sortedBy { it.title }
    }

    suspend fun getTitle(identifier: String): String {
        return room.itemSetDao().getProfileSetTitle(identifier = identifier)
            .orEmpty()
            .ifEmpty { room.itemSetDao().getTopicSetTitle(identifier = identifier).orEmpty() }
    }

    private suspend fun getProfilesFromList(identifier: String): List<AdvancedProfileView> {
        val known = room.profileDao().getAdvancedProfilesOfList(identifier = identifier)
        val unknown = room.profileDao().getUnknownPubkeysFromList(identifier = identifier)
        val friendPubkeys = friendProvider.getFriendPubkeys()
        val mutedPubkeys = room.muteDao().getMyProfileMutes()

        return known + unknown.map { unknownPubkey ->
            createAdvancedProfile(
                pubkey = unknownPubkey,
                dbProfile = null,
                forcedFollowState = friendPubkeys.contains(unknownPubkey),
                forcedMuteState = mutedPubkeys.contains(unknownPubkey),
                metadata = null,
                myPubkey = pubkeyProvider.getPubkeyHex(),
                friendProvider = friendProvider,
                muteProvider = muteProvider,
                itemSetProvider = this
            )
        }
    }

    suspend fun getTopicsFromList(identifier: String, limit: Int = Int.MAX_VALUE): List<Topic> {
        return room.topicDao().getTopicsFromList(identifier = identifier, limit = limit)
    }

    suspend fun getPubkeysFromList(
        identifier: String,
        limit: Int = Int.MAX_VALUE
    ): List<PubkeyHex> {
        return room.itemSetDao().getPubkeys(identifier = identifier, limit = limit)
    }

    fun isInAnySet(pubkey: PubkeyHex): Boolean {
        return allPubkeys.value.contains(pubkey)
    }
}
