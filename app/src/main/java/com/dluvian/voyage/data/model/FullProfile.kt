package com.dluvian.voyage.data.model

import com.dluvian.voyage.data.room.view.AdvancedProfileView

data class FullProfile(
    val inner: AdvancedProfileView = AdvancedProfileView(),
    val about: String? = null,
)
