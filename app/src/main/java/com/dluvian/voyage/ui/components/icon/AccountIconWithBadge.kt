package com.dluvian.voyage.ui.components.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dluvian.voyage.core.PubkeyHex
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.ui.components.indicator.TrustBadgeIndicator
import com.dluvian.voyage.ui.theme.ProfilePictureApricot
import com.dluvian.voyage.ui.theme.ProfilePictureBeige
import com.dluvian.voyage.ui.theme.ProfilePictureBlue
import com.dluvian.voyage.ui.theme.ProfilePictureBrown
import com.dluvian.voyage.ui.theme.ProfilePictureCyan
import com.dluvian.voyage.ui.theme.ProfilePictureGreen
import com.dluvian.voyage.ui.theme.ProfilePictureLavender
import com.dluvian.voyage.ui.theme.ProfilePictureLime
import com.dluvian.voyage.ui.theme.ProfilePictureMagenta
import com.dluvian.voyage.ui.theme.ProfilePictureMaroon
import com.dluvian.voyage.ui.theme.ProfilePictureMint
import com.dluvian.voyage.ui.theme.ProfilePictureNavy
import com.dluvian.voyage.ui.theme.ProfilePictureOlive
import com.dluvian.voyage.ui.theme.ProfilePictureOrange
import com.dluvian.voyage.ui.theme.ProfilePicturePink
import com.dluvian.voyage.ui.theme.ProfilePicturePurple
import com.dluvian.voyage.ui.theme.ProfilePictureRed
import com.dluvian.voyage.ui.theme.ProfilePictureTeal
import com.dluvian.voyage.ui.theme.ProfilePictureYellow

@Composable
fun AccountIconWithBadge(
    pubkey: PubkeyHex,
    trustType: TrustType,
    isSmall: Boolean,
    height: Dp = ButtonDefaults.MinHeight.minus(12.dp)
) {
    Box(
        Modifier
            .height(height)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Identicon(pubkey = pubkey)
        TrustBadgeIndicator(trustType = trustType, isSmall = isSmall)
    }
}

@Stable
@Composable
private fun Identicon(pubkey: PubkeyHex) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .fillMaxSize()
            .background(brush = getDefaultPictureBrush(pubkey = pubkey))
    )
}


private val bottomLeft = Offset(0f, Float.POSITIVE_INFINITY)
private val topRight = Offset(Float.POSITIVE_INFINITY, 0f)
private const val MAX_HEX_LEN = 7

@Stable
@Composable
fun getDefaultPictureBrush(pubkey: String): Brush {
    if (pubkey.isBlank() || pubkey.length < MAX_HEX_LEN) return SolidColor(Color.Transparent)

    val firstNumber = pubkey
        .take(MAX_HEX_LEN)
        .toInt(radix = 16)
    val secondNumber = pubkey
        .takeLast(MAX_HEX_LEN)
        .toInt(radix = 16)

    val gradient = listOf(getColor(firstNumber), getColor(secondNumber))

    return when (secondNumber % 4) {
        0 -> Brush.linearGradient(gradient)
        1 -> Brush.linearGradient(
            colors = gradient,
            start = bottomLeft,
            end = topRight,
        )

        2 -> Brush.horizontalGradient(gradient)
        else -> Brush.verticalGradient(gradient)
    }
}

private fun getColor(number: Int): Color {
    return when (number % 19) {
        0 -> ProfilePictureRed
        1 -> ProfilePictureGreen
        2 -> ProfilePictureYellow
        3 -> ProfilePictureBlue
        4 -> ProfilePictureOrange
        5 -> ProfilePicturePurple
        6 -> ProfilePictureCyan
        7 -> ProfilePictureMagenta
        8 -> ProfilePictureLime
        9 -> ProfilePicturePink
        10 -> ProfilePictureTeal
        11 -> ProfilePictureLavender
        12 -> ProfilePictureBrown
        13 -> ProfilePictureMint
        14 -> ProfilePictureOlive
        15 -> ProfilePictureApricot
        16 -> ProfilePictureMaroon
        17 -> ProfilePictureNavy
        else -> ProfilePictureBeige
    }
}
