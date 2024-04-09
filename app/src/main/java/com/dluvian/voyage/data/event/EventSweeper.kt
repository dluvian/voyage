package com.dluvian.voyage.data.event

import android.util.Log
import com.dluvian.voyage.core.launchIO
import com.dluvian.voyage.data.preferences.DatabasePreferences
import com.dluvian.voyage.data.room.dao.DeleteDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private const val TAG = "EventSweeper"

class EventSweeper(
    private val databasePreferences: DatabasePreferences,
    private val eventCacheClearer: EventCacheClearer,
    private val deleteDao: DeleteDao,
    private val oldestUsedEvent: OldestUsedEvent,
) {
    val scope = CoroutineScope(Dispatchers.IO)

    fun sweep() {
        Log.i(TAG, "Sweep events")

        scope.launchIO {
            sweepRootPostThreshold()
            eventCacheClearer.clear()
            oldestUsedEvent.reset()
        }.invokeOnCompletion {
            Log.i(TAG, "Finished sweeping events", it)
        }
    }

    private suspend fun sweepRootPostThreshold() {
        deleteDao.deleteOldestRootPosts(
            threshold = databasePreferences.getSweepThreshold(),
            oldestCreatedAtInUse = oldestUsedEvent.getOldestCreatedAt()
        )
    }
}
