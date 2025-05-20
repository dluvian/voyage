package com.dluvian.voyage.data.model

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.voyage.data.room.view.AdvancedProfileView

data class FullProfileUI(
    val inner: AdvancedProfileView = AdvancedProfileView(),
    val about: AnnotatedString? = null,
    val lightning: String? = null,
)
