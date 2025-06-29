package actual.core.ui

import actual.budget.model.Amount
import actual.budget.model.NumberFormatConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

val LocalNumberFormatConfig = compositionLocalOf<NumberFormatConfig> { error("No NumberFormatConfig value provided") }

@Composable
fun Amount.formattedString(
  config: NumberFormatConfig = LocalNumberFormatConfig.current,
  includeSign: Boolean = false,
): String = toString(config, includeSign)
