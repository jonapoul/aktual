package aktual.budget.rules.ui.list

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle

@Immutable internal data class RuleSpanStyles(val default: SpanStyle, val highlighted: SpanStyle)

@Composable
internal fun rememberRuleSpanStyles(
  theme: Theme = LocalTheme.current,
  typography: Typography = AktualTypography,
): RuleSpanStyles =
  remember(theme, typography) {
    RuleSpanStyles(
      default = typography.labelMedium.copy(color = theme.pillText).toSpanStyle(),
      highlighted = typography.labelMedium.copy(color = theme.pillTextHighlighted).toSpanStyle(),
    )
  }
