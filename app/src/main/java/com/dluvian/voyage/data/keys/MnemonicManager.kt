package com.dluvian.voyage.data.keys

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dluvian.nostr_kt.generateMnemonic
import rust.nostr.protocol.EventId
import rust.nostr.protocol.Keys


typealias Mnemonic = String

private const val MNEMONIC = "mnemonic"
private const val FILENAME = "voyage_encrypted_mnemonic"
private const val POSTING_ACCOUNT = 50000u
private const val REPLY_SECTION_ACCOUNT = 50001u
private const val MAIN_ACCOUNT_INDEX = 0u

class MnemonicManager(context: Context) {
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

    fun getPostingKeys(time: Long): Keys {
        return deriveKeysFromMnemonic(account = POSTING_ACCOUNT)
    }

    fun getReplySectionKeys(rootId: EventId, rootCreatedAt: Long): Keys {
        return deriveKeysFromMnemonic(account = REPLY_SECTION_ACCOUNT)
    }

    fun getMainAccountKeys(): Keys {
        return deriveKeysFromMnemonic(MAIN_ACCOUNT_INDEX)
    }

    private fun deriveKeysFromMnemonic(account: UInt): Keys {
        return Keys.fromMnemonic(
            mnemonic = getMnemonic() ?: throw IllegalStateException("No mnemonic saved"),
            passphrase = null,
            account = account
        )
    }

    private fun getMnemonic(): Mnemonic? = sharedPreferences.getString(MNEMONIC, null)
}
