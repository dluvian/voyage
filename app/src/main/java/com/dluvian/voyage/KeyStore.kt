package com.dluvian.voyage

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import cash.z.ecc.android.bip39.Mnemonics
import rust.nostr.sdk.Keys
import rust.nostr.sdk.NostrConnect
import rust.nostr.sdk.NostrConnectUri
import rust.nostr.sdk.NostrSigner
import java.security.SecureRandom
import java.time.Duration

class KeyStore(context: Context) {
    private val logTag = "KeyStore"
    private val fileName = "voyage_key_store"
    private val key = "secret"
    private val masterKey: MasterKey =
        MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    val store = EncryptedSharedPreferences.create(
        context, fileName, masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    // Use encryptedSheredPreference anyway
    fun getSigner(): NostrSigner {
        val secret = runCatching { getSecretStr() }.getOrNull()
        if (secret == null) {
            Log.i(logTag, "No secret in found. Generating mnemonic...")
            val mnemonic = generateMnemonic()
            val signer = setSigner(mnemonic)
            return signer ?: throw InvalidSecretException()
        }

        return parseSecretStr(secret) ?: throw InvalidSecretException()
    }

    fun getSecretStr(): String {
        return store.getString(key, null) ?: throw InvalidSecretException()
    }

    // Return null if secret is invalid
    fun setSigner(secret: String): NostrSigner? {
        val parsed = parseSecretStr(secret)
        if (parsed == null) {
            Log.i(logTag, "Can't set invalid secret")
            return null
        }
        store.edit().putString(key, secret).apply()

        return parsed
    }

    private fun generateMnemonic(): String {
        val random = SecureRandom()
        val entropy = ByteArray(16)
        random.nextBytes(entropy)

        return Mnemonics.MnemonicCode(entropy).words.joinToString(separator = " ") { String(it) }
    }

    private fun parseSecretStr(secret: String): NostrSigner? {
        val mnemonicKeys = runCatching { Keys.fromMnemonic(secret, null, 0u) }.getOrNull()
        if (mnemonicKeys != null) {
            Log.i(logTag, "Found mnemonic")
            return NostrSigner.keys(mnemonicKeys)
        }

        val nsecKeys = runCatching { Keys.parse(secret) }.getOrNull()
        if (nsecKeys != null) {
            Log.i(logTag, "Found nsec")
            return NostrSigner.keys(nsecKeys)
        }

        val connect = runCatching { NostrConnectUri.parse(secret) }.getOrNull()
        if (connect != null) {
            Log.i(logTag, "Found nostr connect")
            val parsed = NostrConnect(connect, Keys.generate(), Duration.ofSeconds(5), null)
            return NostrSigner.nostrConnect(parsed)
        }

        Log.i(logTag, "Found invalid secret str")
        return null
    }
}