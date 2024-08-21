package com.dluvian.voyage.data.preferences

import android.content.Context
import com.dluvian.voyage.data.model.FriendPubkeysNoLock
import com.dluvian.voyage.data.model.Global
import com.dluvian.voyage.data.model.InboxFeedSetting
import com.dluvian.voyage.data.model.NoPubkeys
import com.dluvian.voyage.data.model.WebOfTrustPubkeys

private const val PUBKEYS = "pubkeys"
private const val FRIENDS = "friends"
private const val WEB_OF_TRUST = "web_of_trust"
private const val GLOBAL = "global"

class InboxPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(INBOX_FILE, Context.MODE_PRIVATE)

    fun getInboxFeedSetting(): InboxFeedSetting {
        val pubkeys = when (preferences.getString(PUBKEYS, GLOBAL)) {
            FRIENDS -> FriendPubkeysNoLock
            WEB_OF_TRUST -> WebOfTrustPubkeys
            GLOBAL -> Global
            else -> Global
        }
        return InboxFeedSetting(pubkeySelection = pubkeys)
    }

    fun setInboxFeedSettings(setting: InboxFeedSetting) {
        val pubkeys = when (setting.pubkeySelection) {
            FriendPubkeysNoLock -> FRIENDS
            WebOfTrustPubkeys -> WEB_OF_TRUST
            Global -> GLOBAL
            // For some reason I can't model PubkeySelection like:
            //
            // InboxPubkeySelection(friends, wot, global): PubkeySelection()
            // HomePubkeySelection(noPubkeys): InboxPubkeySelection()
            //
            // Can't mix those in a when() switch in HomePreferences. So we default to GLOBAL
            NoPubkeys -> GLOBAL
        }
        preferences.edit()
            .putString(PUBKEYS, pubkeys)
            .apply()
    }
}
