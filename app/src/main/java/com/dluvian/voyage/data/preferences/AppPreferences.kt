package com.dluvian.voyage.data.preferences

import android.content.Context
import androidx.compose.runtime.mutableStateOf

private const val SHOW_AUTHOR_NAME = "show_author_name"

class AppPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(APP_FILE, Context.MODE_PRIVATE)

    val showAuthorNameState = mutableStateOf(getShowAuthorName())

    private fun getShowAuthorName(): Boolean {
        return preferences.getBoolean(SHOW_AUTHOR_NAME, false)
    }

    fun setShowAuthorName(showAuthorName: Boolean) {
        showAuthorNameState.value = showAuthorName
        preferences.edit()
            .putBoolean(SHOW_AUTHOR_NAME, showAuthorName)
            .apply()
    }
}
