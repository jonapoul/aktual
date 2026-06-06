package aktual.budget.rules.ui.list

import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme
import aktual.core.ui.AktualTypography
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle

@Immutable internal data class RuleSpanStyles(val default: SpanStyle, val highlighted: SpanStyle)

@Composable
internal fun rememberRuleSpanStyles(
  colors: Colors = AktualTheme.colors,
  typography: Typography = AktualTypography,
): RuleSpanStyles =
  remember(colors, typography) {
    RuleSpanStyles(
      default = typography.labelMedium.copy(color = colors.pillText).toSpanStyle(),
      highlighted = typography.labelMedium.copy(color = colors.pillTextHighlighted).toSpanStyle(),
    )
  }
