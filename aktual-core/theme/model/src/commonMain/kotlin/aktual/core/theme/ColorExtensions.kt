package aktual.core.theme

import androidx.compose.ui.graphics.Color

internal fun Color.withOpacity(fraction: Number): Color = copy(alpha = alpha * fraction.toFloat())
