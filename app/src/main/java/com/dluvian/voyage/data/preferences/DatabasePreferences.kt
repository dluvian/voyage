package com.dluvian.voyage.data.preferences

import android.content.Context

const val SWEEP_THRESHOLD = "sweep_threshold"
const val DEFAULT_SWEEP_THRESHOLD = 250

class DatabasePreferences(context: Context) {
    private val preferences = context.getSharedPreferences(DATABASE_FILE, Context.MODE_PRIVATE)

    fun getSweepThreshold(): Int {
        return preferences.getInt(SWEEP_THRESHOLD, DEFAULT_SWEEP_THRESHOLD)
    }

    fun setSweepThreshold(newThreshold: Int) {
        preferences.edit().putInt(SWEEP_THRESHOLD, newThreshold).apply()
    }
}
