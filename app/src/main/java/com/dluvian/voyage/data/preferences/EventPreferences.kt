package com.dluvian.voyage.data.preferences

import android.content.Context


private const val UPVOTE_CONTENT = "upvote_content"
private const val DEFAULT_UPVOTE_CONTENT = "+"

class EventPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(DATABASE_FILE, Context.MODE_PRIVATE)

    fun getUpvoteContent(): String {
        return preferences.getString(UPVOTE_CONTENT, DEFAULT_UPVOTE_CONTENT)
            ?: DEFAULT_UPVOTE_CONTENT
    }

    fun setUpvoteContent(newUpvote: String) {
        preferences.edit()
            .putString(UPVOTE_CONTENT, newUpvote)
            .apply()
    }
}