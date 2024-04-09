package com.dluvian.voyage.data.preferences

import android.content.Context
import com.dluvian.voyage.core.DEFAULT_RETAIN_ROOT
import com.dluvian.voyage.core.MIN_RETAIN_ROOT

const val SWEEP_THRESHOLD = "sweep_threshold"

class DatabasePreferences(context: Context) {
    private val preferences = context.getSharedPreferences(DATABASE_FILE, Context.MODE_PRIVATE)

    fun getSweepThreshold(): Int {
        return maxOf(
            preferences.getInt(SWEEP_THRESHOLD, DEFAULT_RETAIN_ROOT),
            MIN_RETAIN_ROOT.toInt()
        )
    }

    fun setSweepThreshold(newThreshold: Int) {
        preferences.edit()
            .putInt(SWEEP_THRESHOLD, maxOf(newThreshold, MIN_RETAIN_ROOT.toInt()))
            .apply()
    }
}
