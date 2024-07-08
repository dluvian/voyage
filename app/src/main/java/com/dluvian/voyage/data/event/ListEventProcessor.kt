package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.data.account.IPubkeyProvider
import com.dluvian.voyage.data.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "ListEventProcessor"

class ListEventProcessor(
    private val scope: CoroutineScope,
    private val pubkeyProvider: IPubkeyProvider,
    private val room: AppDatabase,
) {
    fun processLists(lists: Collection<ValidatedList>) {
        if (lists.isEmpty()) return

        val nip65s = mutableListOf<ValidatedNip65>()
        val contactLists = mutableListOf<ValidatedContactList>()
        val topicLists = mutableListOf<ValidatedTopicList>()
        val bookmarkLists = mutableListOf<ValidatedBookmarkList>()
        val muteLists = mutableListOf<ValidatedMuteList>()

        lists.forEach { list ->
            when (list) {
                is ValidatedNip65 -> nip65s.add(list)
                is ValidatedContactList -> contactLists.add(list)
                is ValidatedTopicList -> topicLists.add(list)
                is ValidatedBookmarkList -> bookmarkLists.add(list)
                is ValidatedMuteList -> muteLists.add(list)
            }
        }

        processContactLists(contactLists = contactLists)
        processNip65s(nip65s = nip65s)
        processTopicLists(topicLists = topicLists)
        processBookmarkLists(bookmarkLists = bookmarkLists)
        processMuteLists(muteLists = muteLists)
    }

    private fun processContactLists(contactLists: Collection<ValidatedContactList>) {
        if (contactLists.isEmpty()) return

        val newestLists = filterNewestLists(lists = contactLists)

        val myPubkey = pubkeyProvider.getPubkeyHex()
        val myList = newestLists.find { it.pubkey == myPubkey }
        val otherLists = newestLists.let { if (myList != null) it - myList else it }

        processFriendList(myFriendList = myList)
        processWebOfTrustList(validatedWoTs = otherLists)
    }

    private fun processFriendList(myFriendList: ValidatedContactList?) {
        if (myFriendList == null) return

        scope.launch {
            Log.d(TAG, "Upsert my friend list of ${myFriendList.friendPubkeys.size} friends")
            room.friendUpsertDao().upsertFriends(validatedContactList = myFriendList)
        }.invokeOnCompletion { exception ->
            if (exception != null) Log.w(TAG, "Failed to process friend list", exception)
        }
    }

    private fun processWebOfTrustList(validatedWoTs: Collection<ValidatedContactList>) {
        if (validatedWoTs.isEmpty()) return

        scope.launch {
            Log.d(TAG, "Upsert ${validatedWoTs.size} wot contact lists")
            room.webOfTrustUpsertDao().upsertWebOfTrust(validatedWoTs = validatedWoTs)
        }.invokeOnCompletion { ex ->
            if (ex != null) Log.w(TAG, "Failed to process web of trust list", ex)
        }
    }

    private fun processNip65s(nip65s: Collection<ValidatedNip65>) {
        if (nip65s.isEmpty()) return

        val newestNip65s = filterNewestLists(lists = nip65s).filter { it.relays.isNotEmpty() }
        if (newestNip65s.isEmpty()) return

        scope.launch {
            Log.d(TAG, "Upsert ${nip65s.size} nip65s")
            room.nip65UpsertDao().upsertNip65s(validatedNip65s = newestNip65s)
        }.invokeOnCompletion { ex ->
            if (ex != null) Log.w(TAG, "Failed to process nip65", ex)
        }
    }

    private fun processTopicLists(topicLists: Collection<ValidatedTopicList>) {
        if (topicLists.isEmpty()) return

        val myNewestList = filterNewestLists(lists = topicLists)
            .firstOrNull { it.myPubkey == pubkeyProvider.getPubkeyHex() } ?: return

        scope.launch {
            Log.d(TAG, "Upsert topic list of ${myNewestList.topics.size} topics")
            room.topicUpsertDao().upsertTopics(validatedTopicList = myNewestList)
        }
    }

    private fun processBookmarkLists(bookmarkLists: Collection<ValidatedBookmarkList>) {
        if (bookmarkLists.isEmpty()) return

        val myNewestList = filterNewestLists(lists = bookmarkLists)
            .firstOrNull { it.myPubkey == pubkeyProvider.getPubkeyHex() } ?: return

        scope.launch {
            Log.d(TAG, "Upsert bookmark list of ${myNewestList.postIds.size} postIds")
            room.bookmarkUpsertDao().upsertBookmarks(validatedBookmarkList = myNewestList)
        }
    }

    private fun processMuteLists(muteLists: Collection<ValidatedMuteList>) {
        if (muteLists.isEmpty()) return

        val myNewestList = filterNewestLists(lists = muteLists)
            .firstOrNull { it.myPubkey == pubkeyProvider.getPubkeyHex() } ?: return

        scope.launch {
            Log.d(TAG, "Upsert mute list")
            room.muteUpsertDao().upsertMuteList(muteList = myNewestList)
        }
    }

    private fun <T : ValidatedList> filterNewestLists(lists: Collection<T>): List<T> {
        return lists.sortedByDescending { it.createdAt }.distinctBy { it.owner }
    }
}
