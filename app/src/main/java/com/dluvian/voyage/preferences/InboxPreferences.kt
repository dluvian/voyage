package com.dluvian.voyage.preferences

import android.content.Context
import com.dluvian.voyage.data.filterSetting.FriendPubkeys
import com.dluvian.voyage.data.filterSetting.Global
import com.dluvian.voyage.data.filterSetting.NoPubkeys
import com.dluvian.voyage.data.filterSetting.WebOfTrustPubkeys
import com.dluvian.voyage.filterSetting.InboxFeedSetting

private const val PUBKEYS = "pubkeys"
private const val FRIENDS = "friends"
private const val WEB_OF_TRUST = "web_of_trust"
private const val GLOBAL = "global"

class InboxPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(INBOX_FILE, Context.MODE_PRIVATE)

    fun getInboxFeedSetting(): InboxFeedSetting {
        val pubkeys = when (preferences.getString(PUBKEYS, GLOBAL)) {
            FRIENDS -> FriendPubkeys
            WEB_OF_TRUST -> WebOfTrustPubkeys
            GLOBAL -> Global
            else -> Global
        }
        return InboxFeedSetting(pubkeySelection = pubkeys)
    }

    fun setInboxFeedSettings(setting: InboxFeedSetting) {
        val pubkeys = when (setting.pubkeySelection) {
            FriendPubkeys -> FRIENDS
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
