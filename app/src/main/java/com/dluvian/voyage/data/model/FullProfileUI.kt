package com.dluvian.voyage.data.model

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.core.Bech32
import com.dluvian.voyage.data.room.view.AdvancedProfileView

data class FullProfileUI(
    val inner: AdvancedProfileView = AdvancedProfileView(),
    val npub: Bech32 = "",
    val about: AnnotatedString? = null,
    val lightning: String? = null,
)
