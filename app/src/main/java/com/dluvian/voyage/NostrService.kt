package com.dluvian.voyage

import com.dluvian.voyage.preferences.RelayPreferences
import rust.nostr.sdk.ClientBuilder
import rust.nostr.sdk.Event
import rust.nostr.sdk.EventBuilder
import rust.nostr.sdk.Events
import rust.nostr.sdk.Filter
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.NostrDatabase
import rust.nostr.sdk.Options
import rust.nostr.sdk.PublicKey
import rust.nostr.sdk.RelayMetadata
import rust.nostr.sdk.RelayOptions
import rust.nostr.sdk.ReqExitPolicy
import rust.nostr.sdk.SendEventOutput
import rust.nostr.sdk.SubscribeAutoCloseOptions
import rust.nostr.sdk.SubscribeOutput
import rust.nostr.sdk.extractRelayList

class NostrService(
    relayPreferences: RelayPreferences,
    keyStore: KeyStore,
) {
    private val clientOpts = Options()
        .gossip(true)
        .automaticAuthentication(relayPreferences.getSendAuth())

    val client = ClientBuilder()
        .signer(keyStore.getSigner())
        .opts(clientOpts)
        .database(NostrDatabase.lmdb("voyage_db"))
        .admitPolicy(RelayChecker())
        .build()

    suspend fun init() {
        getPersonalRelays().forEach { (relay, meta) ->
            val opts = RelayOptions()
                .read(meta == null || meta == RelayMetadata.READ)
                .write(meta == null || meta == RelayMetadata.WRITE)
            client.addRelayWithOpts(url = relay, opts = opts)
        }

        client.connect()
    }

    // TODO: No RelayUrl struct in nostr-sdk ?
    private suspend fun getPersonalRelays(): Map<RelayUrl, RelayMetadata?> {
        val filter = Filter()
            .author(client.signer().getPublicKey())
            .kind(Kind.fromStd(KindStandard.RELAY_LIST))
            .limit(1u)
        val event = dbQuery(filter).first()

        return if (event != null) {
            extractRelayList(event)
        } else {
            mapOf(
                Pair("wss://nos.lol", null),
                Pair("wss://relay.damus.io", null),
                Pair("wss://relay.primal.net", null)
            )
        }
    }

    suspend fun rebroadcast(event: Event): SendEventOutput {
        val relays = client.relays().keys.toList()

        return client.sendEventTo(urls = relays, event = event)
    }

    suspend fun publish(event: Event): SendEventOutput {
        return client.sendEvent(event)
    }

    suspend fun sign(builder: EventBuilder): Result<Event> {
        return runCatching { client.signEventBuilder(builder) }
    }

    suspend fun pubkey(): PublicKey {
        return client.signer().getPublicKey()
    }

    suspend fun dbQuery(filter: Filter): Events {
        return client.database().query(filter)
    }

    suspend fun dbDelete(filter: Filter) {
        return client.database().delete(filter)
    }

    suspend fun subscribe(filter: Filter): SubscribeOutput {
        val opts = SubscribeAutoCloseOptions().exitPolicy(ReqExitPolicy.ExitOnEose)

        return client.subscribe(filter = filter, opts = opts)
    }

    suspend fun close() {
        client.shutdown()
    }
}
