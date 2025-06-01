package com.dluvian.voyage.provider

import android.util.Log
import com.dluvian.voyage.Ident
import com.dluvian.voyage.NostrService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rust.nostr.sdk.Event
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.Timestamp
import kotlin.collections.orEmpty


class TrustProvider(private val service: NostrService) {
    private val logTag = "TrustProvider"
    private val mutex = Mutex()
    private var friendEvent: Event? = null
    private var web = mutableMapOf<PublicKey, Pair<Timestamp, Set<PublicKey>>>()
    private var lists = mutableMapOf<Ident, Pair<Timestamp, Set<PublicKey>>>()

    suspend fun init() {
        val pubkey = service.pubkey()
        val kinds = listOf(KindStandard.CONTACT_LIST, KindStandard.FOLLOW_SET)
            .map { Kind.fromStd(it) }
        val myFilter = Filter()
            .author(pubkey)
            .kinds(kinds)

        val dbResult = service.dbQuery(myFilter).toVec()
        if (dbResult.isEmpty()) {
            Log.i(logTag, "Trust data of $pubkey is not found in database")
            return
        }

        val listPairs = dbResult
            .filter { it.kind() == Kind.fromStd(KindStandard.FOLLOW_SET) }
            .map { Pair(it.tags().identifier().orEmpty(), it.toPair()) }
        if (listPairs.isNotEmpty()) {
            mutex.withLock { lists.putAll(listPairs) }
        }

        val friends = dbResult.firstOrNull { it.kind() == Kind.fromStd(KindStandard.CONTACT_LIST) }
        if (friends != null) {
            mutex.withLock { friendEvent = friends }
        }
    }

    suspend fun switchSigner() {
        mutex.withLock {
            friendEvent = null
            web.clear()
            lists.clear()
        }
        init()
    }

    suspend fun update(event: Event) {
        val pubkey = service.pubkey()
        when (event.kind().asStd()) {
            KindStandard.EVENT_DELETION -> {
                if (event.author() != pubkey) return
                val idents = event.tags()
                    .coordinates()
                    .filter { it.publicKey() == pubkey }
                    .filter { it.kind() == Kind.fromStd(KindStandard.FOLLOW_SET) }
                    .map { it.identifier() }
                mutex.withLock {
                    idents.forEach { ident -> lists.remove(ident) }
                }
            }

            KindStandard.CONTACT_LIST -> {
                if (event.author() == pubkey) {
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

            KindStandard.FOLLOW_SET -> {
                if (event.author() != pubkey) return
                val ident = event.tags().identifier()
                if (ident == null) return

                val currentTime = mutex.withLock { lists[ident]?.first?.asSecs() ?: 0u }
                if (event.createdAt().asSecs() <= currentTime) return // TODO: Wait for compare
                mutex.withLock { lists[ident] = event.toPair() }
            }

            null -> {
                Log.w(logTag, "${event.kind().asU16()} has no KindStandard")
                return
            }

            else -> {
                Log.d(logTag, "Updating ${event.kind().asU16()} is not supported")
                return
            }
        }
    }

    suspend fun friends(): List<PublicKey> {
        return mutex.withLock { friendEvent?.tags()?.publicKeys().orEmpty() }
    }

    suspend fun filterWebTrust(pubkeys: Collection<PublicKey>): List<PublicKey> {
        val webPubkeys = webPubkeys()

        return pubkeys.filter { webPubkeys.contains(it) }
    }

    suspend fun reserveWeb(pubkeys: Collection<PublicKey>) {
        val inWeb = filterWebTrust(pubkeys).toSet()
        val missing = pubkeys.filterNot { inWeb.contains(it) }
        if (missing.isEmpty()) return

        val contactFilter = Filter()
            .kind(Kind.fromStd(KindStandard.CONTACT_LIST))
            .pubkeys(missing) // Referenced as p-tag
            .limit(missing.size.toULong())
        val dbResult = service.dbQuery(contactFilter).toVec()
        dbResult.forEach { event -> update(event) }
        val dbPubkeys = dbResult.flatMap { it.tags().publicKeys() }.toSet()

        val dbMissing = missing.filterNot { dbPubkeys.contains(it) }
        if (dbMissing.isEmpty()) return

        val subFilter = Filter()
            .kind(Kind.fromStd(KindStandard.CONTACT_LIST))
            .authors(friends())
            .pubkeys(dbMissing) // Referenced as p-tag
            .limit(dbMissing.size.toULong())
        service.subscribe(subFilter)
    }

    suspend fun filterListTrust(pubkeys: Collection<PublicKey>): List<PublicKey> {
        val listPubkeys = mutex.withLock { lists.values }
            .flatMap { (_, pubkeys) -> pubkeys }
            .toSet()

        return pubkeys.filter { listPubkeys.contains(it) }
    }

    private fun Event.toPair(): Pair<Timestamp, Set<PublicKey>> {
        return Pair(this.createdAt(), this.tags().publicKeys().toSet())
    }

    private suspend fun webPubkeys(): Set<PublicKey> {
        return mutex.withLock { web.values }
            .flatMap { (_, pubkeys) -> pubkeys }
            .toSet()
    }
}