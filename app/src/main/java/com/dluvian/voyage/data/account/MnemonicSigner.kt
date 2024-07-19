package com.dluvian.voyage.data.account

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dluvian.voyage.data.nostr.generateMnemonic
import rust.nostr.protocol.Event
import rust.nostr.protocol.Keys
import rust.nostr.protocol.PublicKey
import rust.nostr.protocol.UnsignedEvent


typealias Mnemonic = String

private const val MNEMONIC = "mnemonic"
private const val FILENAME = "voyage_encrypted_mnemonic"
private const val MAIN_ACCOUNT_INDEX = 0u

class MnemonicSigner(context: Context) : IMyPubkeyProvider {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Initialize EncryptedSharedPreferences
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILENAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    init {
        if (getMnemonic() == null) {
            sharedPreferences.edit()
                .putString(MNEMONIC, generateMnemonic())
                .apply()
        }
    }

    override fun getPublicKey(): PublicKey {
        return deriveMainAccount().publicKey()
    }

    fun sign(unsignedEvent: UnsignedEvent): Result<Event> {
        val keys = deriveMainAccount()

        if (unsignedEvent.author() != keys.publicKey()) {
            val err = "Author of unsigned event ${unsignedEvent.author().toHex()} " +
                    "does not match mnemonic main account ${keys.publicKey().toHex()}"
            return Result.failure(IllegalArgumentException(err))
        }

        return runCatching { unsignedEvent.sign(keys) }
    }

    fun getSeed() = getMnemonic()?.split(" ").orEmpty()

    private fun deriveMainAccount(): Keys {
        return Keys.fromMnemonic(
            mnemonic = getMnemonic() ?: throw IllegalStateException("No mnemonic saved"),
            passphrase = null,
            account = MAIN_ACCOUNT_INDEX
        )
    }

    private fun getMnemonic(): Mnemonic? = sharedPreferences.getString(MNEMONIC, null)
}
