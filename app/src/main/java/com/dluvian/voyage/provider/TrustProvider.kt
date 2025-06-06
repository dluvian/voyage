package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.model.FollowedProfile
import com.dluvian.voyage.model.OneselfProfile
import com.dluvian.voyage.model.TrustProfile
import com.dluvian.voyage.model.TrustedProfile
import com.dluvian.voyage.model.UnknownProfile
import com.dluvian.voyage.nostr.NostrService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.Timestamp
import kotlin.collections.orEmpty


class TrustProvider(private val service: NostrService) : IEventUpdate {
    private val logTag = "TrustProvider"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()
    private var friendEvent: Event? = null
    private val web = mutableMapOf<PublicKey, Pair<Timestamp, Set<PublicKey>>>()

    suspend fun init() {
        val pubkey = service.pubkey()
        val myFilter = Filter()
            .author(pubkey)
            .kind(Kind.fromStd(KindStandard.CONTACT_LIST))

        val dbResult = service.dbQuery(myFilter)
        if (dbResult.isEmpty()) {
            Log.i(logTag, "Trust data of $pubkey is not found in database")
            return
        }

        val friends = dbResult.firstOrNull { it.kind() == Kind.fromStd(KindStandard.CONTACT_LIST) }
        if (friends != null) {
            mutex.withLock { friendEvent = friends }
        }
    }

    // TODO: Ayooo
    suspend fun switchSigner() {
        mutex.withLock {
            friendEvent = null
            web.clear()
        }
        init()
    }

    override suspend fun update(event: Event) {
        when (event.kind().asStd()) {
            KindStandard.CONTACT_LIST -> {
                if (event.author() == service.pubkey()) {
                    val currentTime = mutex.withLock { friendEvent?.createdAt()?.asSecs() ?: 0u }
                    if (event.createdAt().asSecs() <= currentTime) return // TODO: Wait for compare
                    val friends = event.tags().publicKeys().toSet()
                    mutex.withLock {
                        friendEvent = event
                        val nonFriends = web.keys.filterNot { friends.contains(it) }
                        nonFriends.forEach { web.remove(it) }
                    }
                    return
                }

                if (!friends().contains(event.author())) return

                val currentTime = mutex.withLock { web[event.author()]?.first?.asSecs() ?: 0u }
                if (event.createdAt().asSecs() <= currentTime) return // TODO: Wait for compare

                // Prevent bloating up web of trust in memory
                // by limiting each pubkey to a single occurrence
                val allPubkeys = webPubkeys()
                val uniquePubkeys = event.tags().publicKeys().filterNot { allPubkeys.contains(it) }
                val webValue = Pair(event.createdAt(), uniquePubkeys.toSet())
                mutex.withLock { web[event.author()] = webValue }
            }

            null -> {
                Log.w(logTag, "${event.kind().asU16()} has no KindStandard")
            }

            else -> {
            }
        }
    }

    suspend fun pubkey(): PublicKey {
        return service.pubkey()
    }

    suspend fun friends(): List<PublicKey> {
        return mutex.withLock { friendEvent?.tags()?.publicKeys().orEmpty() }
    }

    suspend fun getTrustProfiles(pubkeys: Collection<PublicKey>): Map<PublicKey, TrustProfile> {
        val pubkey = service.pubkey()
        val friends = friends()
        val trusted = filterWebTrust(pubkeys).toSet()

        return pubkeys.map {
            if (pubkey == it) Pair(it, OneselfProfile(pubkey = it, name = ""))
            else if (friends.contains(it)) Pair(it, FollowedProfile(pubkey = it, name = ""))
            else if (trusted.contains(it)) Pair(it, TrustedProfile(pubkey = it, name = ""))
            else Pair(it, UnknownProfile(pubkey = it, name = ""))
        }.toMap()
    }

    suspend fun reserveWeb(pubkeys: Collection<PublicKey>, dbOnly: Boolean) {
        val nonFriends = pubkeys - friends()
        if (nonFriends.isEmpty()) return

        val inWeb = filterWebTrust(nonFriends).toSet()
        val missing = nonFriends.filterNot { inWeb.contains(it) }
        if (missing.isEmpty()) return

        val contactFilter = Filter()
            .kind(Kind.fromStd(KindStandard.CONTACT_LIST))
            .pubkeys(missing) // Referenced as p-tag
            .limit(missing.size.toULong())
        val dbResult = service.dbQuery(contactFilter)
        dbResult.forEach { event -> update(event) }
        val dbPubkeys = dbResult.flatMap { it.tags().publicKeys() }.toSet()

        val dbMissing = missing.filterNot { dbPubkeys.contains(it) }
        if (dbMissing.isEmpty()) return

        if (!dbOnly) {
            val subFilter = Filter()
                .kind(Kind.fromStd(KindStandard.CONTACT_LIST))
                .authors(friends())
                .pubkeys(dbMissing) // Referenced as p-tag
                .limit(dbMissing.size.toULong())
            scope.launch {
                service.subscribe(subFilter)
            }
        }
    }

    private suspend fun filterWebTrust(pubkeys: Collection<PublicKey>): List<PublicKey> {
        val webPubkeys = webPubkeys()

        return pubkeys.filter { webPubkeys.contains(it) }
    }

    private suspend fun webPubkeys(): Set<PublicKey> {
        return mutex.withLock { web.values }
            .flatMap { (_, pubkeys) -> pubkeys }
            .toSet()
    }
}