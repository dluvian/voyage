package com.dluvian.voyage

import android.content.Context
import androidx.room.Room
import com.dluvian.nostr_kt.NostrClient
import com.dluvian.nostr_kt.SubId
import com.dluvian.voyage.data.EventMaker
import com.dluvian.voyage.data.EventProcessor
import com.dluvian.voyage.data.NostrService
import com.dluvian.voyage.data.keys.AccountKeyManager
import com.dluvian.voyage.data.keys.SingleUseKeyManager
import com.dluvian.voyage.data.room.AppDatabase
import okhttp3.OkHttpClient
import rust.nostr.protocol.Filter
import java.util.Collections

class AppContainer(context: Context) {
    val roomDb: AppDatabase = Room.databaseBuilder(
        context = context,
        klass = AppDatabase::class.java,
        name = "voyage_database",
    ).build()
    private val client = OkHttpClient()
    private val nostrClient = NostrClient(client)
    private val filterCache = Collections.synchronizedMap(mutableMapOf<SubId, List<Filter>>())
    private val eventProcessor = EventProcessor(filterCache)
    private val singleUseKeyManager = SingleUseKeyManager(context)
    private val accountKeyManager = AccountKeyManager(context)
    private val eventMaker = EventMaker(singleUseKeyManager, accountKeyManager)
    val nostrService = NostrService(nostrClient, eventProcessor, eventMaker, filterCache)
}
