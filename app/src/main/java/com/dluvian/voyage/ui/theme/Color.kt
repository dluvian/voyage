package com.dluvian.voyage.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.dluvian.voyage.core.model.FriendTrust
import com.dluvian.voyage.core.model.IsInListTrust
import com.dluvian.voyage.core.model.Locked
import com.dluvian.voyage.core.model.LockedOneself
import com.dluvian.voyage.core.model.Muted
import com.dluvian.voyage.core.model.NoTrust
import com.dluvian.voyage.core.model.Oneself
import com.dluvian.voyage.core.model.TrustType
import com.dluvian.voyage.core.model.WebTrust

val TallPoppyRed = Color(0xFFB92B27)
val Purple = Color(0xFFb5179e)
val DenimBlue = Color(0xFF1565c0)

val WotColor = Color(0xFFF8D97B)
val FriendColor = Color(0xFFC1FA7F)
val MutedColor = Color(0xFFFA6D6D)
val LockedColor = Color.Red

val HyperlinkBlue = Color(0xFF007AFF)

val md_theme_light_primary = Color(0xFF3B4DD8)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFDFE0FF)
val md_theme_light_onPrimaryContainer = Color(0xFF000B62)
val md_theme_light_secondary = Color(0xFFAB351F)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFFFDAD3)
val md_theme_light_onSecondaryContainer = Color(0xFF3E0400)
val md_theme_light_tertiary = Color(0xFF4756B4)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFDEE0FF)
val md_theme_light_onTertiaryContainer = Color(0xFF000E5E)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFFFBFF)
val md_theme_light_onBackground = Color(0xFF1B1B1F)
val md_theme_light_surface = Color(0xFFFFFBFF)
val md_theme_light_onSurface = Color(0xFF1B1B1F)
val md_theme_light_surfaceVariant = Color(0xFFE3E1EC)
val md_theme_light_onSurfaceVariant = Color(0xFF46464F)
val md_theme_light_outline = Color(0xFF777680)
val md_theme_light_inverseOnSurface = Color(0xFFF3F0F4)
val md_theme_light_inverseSurface = Color(0xFF303034)
val md_theme_light_inversePrimary = Color(0xFFBCC2FF)
val md_theme_light_surfaceTint = Color(0xFF3B4DD8)
val md_theme_light_outlineVariant = Color(0xFFC7C5D0)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFFBCC2FF)
val md_theme_dark_onPrimary = Color(0xFF00179B)
val md_theme_dark_primaryContainer = Color(0xFF1C31C0)
val md_theme_dark_onPrimaryContainer = Color(0xFFDFE0FF)
val md_theme_dark_secondary = Color(0xFFFFB4A5)
val md_theme_dark_onSecondary = Color(0xFF650B00)
val md_theme_dark_secondaryContainer = Color(0xFF891D09)
val md_theme_dark_onSecondaryContainer = Color(0xFFFFDAD3)
val md_theme_dark_tertiary = Color(0xFFBBC3FF)
val md_theme_dark_onTertiary = Color(0xFF112384)
val md_theme_dark_tertiaryContainer = Color(0xFF2D3D9B)
val md_theme_dark_onTertiaryContainer = Color(0xFFDEE0FF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF1B1B1F)
val md_theme_dark_onBackground = Color(0xFFE4E1E6)
val md_theme_dark_surface = Color(0xFF1B1B1F)
val md_theme_dark_onSurface = Color(0xFFE4E1E6)
val md_theme_dark_surfaceVariant = Color(0xFF46464F)
val md_theme_dark_onSurfaceVariant = Color(0xFFC7C5D0)
val md_theme_dark_outline = Color(0xFF91909A)
val md_theme_dark_inverseOnSurface = Color(0xFF1B1B1F)
val md_theme_dark_inverseSurface = Color(0xFFE4E1E6)
val md_theme_dark_inversePrimary = Color(0xFF3B4DD8)
val md_theme_dark_surfaceTint = Color(0xFFBCC2FF)
val md_theme_dark_outlineVariant = Color(0xFF46464F)
val md_theme_dark_scrim = Color(0xFF000000)

fun Color.light(factor: Float = 0.5f) = this.copy(alpha = this.alpha * factor)

val OPBlue: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF388de2) else Color(0xFF244b99)

val OnBgLight: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground.light()

@Stable
@Composable
fun getTrustColor(trustType: TrustType): Color {
    return when (trustType) {
        Oneself -> FriendColor
        FriendTrust, IsInListTrust -> FriendColor
        WebTrust -> WotColor
        Muted -> MutedColor
        Locked, LockedOneself -> LockedColor
        NoTrust -> MaterialTheme.colorScheme.onBackground.light(0.2f)
    }
}
