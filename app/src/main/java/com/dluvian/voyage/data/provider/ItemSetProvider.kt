package com.dluvian.voyage.data.provider

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.core.SHORT_DEBOUNCE
import com.dluvian.voyage.core.Topic
import com.dluvian.voyage.core.createAdvancedProfile
import com.dluvian.voyage.core.firstThenDistinctDebounce
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.model.ItemSetMeta
import com.dluvian.voyage.data.room.dao.ItemSetDao
import com.dluvian.voyage.data.room.dao.ProfileDao
import com.dluvian.voyage.data.room.dao.TopicDao
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ItemSetProvider(
    private val itemSetDao: ItemSetDao,
    private val profileDao: ProfileDao,
    private val topicDao: TopicDao,
    private val pubkeyProvider: IPubkeyProvider,
    private val friendProvider: FriendProvider,
) {
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
            itemSetDao.getMyProfileSetMetasFlow().firstThenDistinctDebounce(SHORT_DEBOUNCE),
            itemSetDao.getMyTopicSetMetasFlow().firstThenDistinctDebounce(SHORT_DEBOUNCE)
        ) { profileSets, topicSets ->
            profileSets.plus(topicSets).distinctBy { it.identifier }.sortedBy { it.title }
        }
    }

    private suspend fun getTitle(identifier: String): String {
        return itemSetDao.getProfileSetTitle(identifier = identifier)
            .orEmpty()
            .ifEmpty { itemSetDao.getTopicSetTitle(identifier = identifier).orEmpty() }
    }

    private suspend fun getProfilesFromList(identifier: String): List<AdvancedProfileView> {
        val known = profileDao.getAdvancedProfilesOfList(identifier = identifier)
        val unknown = profileDao.getUnknownPubkeysFromList(identifier = identifier)
        val friendPubkeys = friendProvider.getFriendPubkeys().toSet()

        return known + unknown.map { unknownPubkey ->
            createAdvancedProfile(
                pubkey = unknownPubkey,
                dbProfile = null,
                forcedFollowState = friendPubkeys.contains(unknownPubkey),
                metadata = null,
                myPubkey = pubkeyProvider.getPubkeyHex(),
                friendProvider = friendProvider,
            )
        }
    }

    private suspend fun getTopicsFromList(identifier: String): List<Topic> {
        return topicDao.getTopicsFromList(identifier = identifier)
    }
}
