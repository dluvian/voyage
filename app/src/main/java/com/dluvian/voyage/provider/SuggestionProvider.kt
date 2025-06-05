package com.dluvian.voyage.provider

import androidx.compose.runtime.mutableStateOf
import com.dluvian.voyage.Topic
import com.dluvian.voyage.model.TrustProfile

class SuggestionProvider {
    val profiles = mutableStateOf(emptyList<TrustProfile>())
    val topics = mutableStateOf(emptyList<Topic>())

    fun lol() {
        TODO()
    }
}
