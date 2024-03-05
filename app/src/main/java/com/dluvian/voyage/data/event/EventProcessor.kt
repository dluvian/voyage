package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.nostr_kt.isContactList
import com.dluvian.nostr_kt.isPostOrReply
import com.dluvian.nostr_kt.isTopicList
import com.dluvian.nostr_kt.isVote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rust.nostr.protocol.Event

private const val TAG = "EventProcessor"
private const val MAX_SQL_PARAMS = 200

class EventProcessor {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun processEvents(events: Set<Event>) {
        if (events.isEmpty()) return
        Log.d(TAG, "Process ${events.size} events")

        val posts = mutableListOf<Event>()
        val votes = mutableListOf<Event>()
        val contactLists = mutableListOf<Event>()
        val topicLists = mutableListOf<Event>()

        events.forEach {
            if (it.isPostOrReply()) posts.add(it)
            else if (it.isVote()) votes.add(it)
            else if (it.isContactList()) contactLists.add(it)
            else if (it.isTopicList()) topicLists.add(it)
        }

        processPosts(events = posts)
        processVotes(events = votes)
        processContactLists(events = contactLists)
        processTopicLists(events = topicLists)
    }

    // TODO: Insert PostRelays
    private fun processPosts(events: Collection<Event>) {
        if (events.isEmpty()) return
        Log.d(TAG, "Process ${events.size} posts")
        // TODO: sort events to satisfy foreign key constraint: root first, then top level reply, ...
        val batches = events.chunked(MAX_SQL_PARAMS)

        batches.forEach { batch ->
            scope.launch {
                TODO("Insert post and eventRelay")
            }.invokeOnCompletion { exception ->
                if (exception != null) {
                    Log.w(TAG, "Failed to process posts", exception)
                    return@invokeOnCompletion
                }
            }
        }
    }

    private fun processVotes(events: Collection<Event>) {
    }

    private fun processContactLists(events: Collection<Event>) {
    }

    private fun processTopicLists(events: Collection<Event>) {
    }
}