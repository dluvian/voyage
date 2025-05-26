package com.dluvian.voyage.data.preferences

import android.content.Context


private const val UPVOTE_CONTENT = "upvote_content"
private const val CLIENT_TAG = "client_tag"
private const val DEFAULT_UPVOTE_CONTENT = "+"

class EventPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(EVENT_FILE, Context.MODE_PRIVATE)

    fun getUpvoteContent(): String {
        return preferences.getString(UPVOTE_CONTENT, DEFAULT_UPVOTE_CONTENT)
            ?: DEFAULT_UPVOTE_CONTENT
    }

    fun setUpvoteContent(newUpvote: String) {
        preferences.edit()
            .putString(UPVOTE_CONTENT, newUpvote)
            .apply()
    }

    fun isAddingClientTag(): Boolean {
        return preferences.getBoolean(CLIENT_TAG, true)
    }

    fun setIsAddingClientTag(addClientTag: Boolean) {
        preferences.edit()
            .putBoolean(CLIENT_TAG, addClientTag)
            .apply()
    }

}
