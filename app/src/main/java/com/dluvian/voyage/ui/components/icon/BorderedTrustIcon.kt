package com.dluvian.voyage.ui.components.icon

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.TrustType

@Composable
fun BorderedTrustIcon(
    pubkey: PubkeyHex,
    trustType: TrustType,
    isCircle: Boolean = false,
    height: Dp = ButtonDefaults.MinHeight.minus(12.dp)
) {
    Box(
        Modifier
            .height(height)
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                brush = getBorder(pubkey = pubkey),
                shape = if (isCircle) CircleShape else RoundedCornerShape(5.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        TrustIcon(modifier = Modifier.padding(3.dp), trustType = trustType)
    }
}

private const val MAX_HEX_LEN = 6

@Stable
@Composable
fun getBorder(pubkey: String): Brush {
    if (pubkey.isBlank() || pubkey.length < MAX_HEX_LEN) return SolidColor(Color.Transparent)

    val firstNumber = ("ff" + pubkey.take(MAX_HEX_LEN)).toLong(radix = 16)
    val secondNumber = ("ff" + pubkey.takeLast(MAX_HEX_LEN)).toLong(radix = 16)

    return Brush.linearGradient(listOf(Color(firstNumber), Color(secondNumber)))
}
