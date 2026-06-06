package aktual.core.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val LocalTheme = staticCompositionLocalOf<Theme> { Theme.Fallback }

@Stable fun Color.isLight(): Boolean = luminance() > LUMINANCE_BOUNDARY

private const val LUMINANCE_BOUNDARY = 0.5f
