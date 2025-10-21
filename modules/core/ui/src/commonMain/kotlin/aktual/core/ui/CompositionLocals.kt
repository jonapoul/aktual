/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui

import aktual.budget.model.Amount
import aktual.budget.model.NumberFormat
import aktual.budget.model.NumberFormatConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalPrivacyEnabled = compositionLocalOf { false }

val LocalNumberFormatConfig = compositionLocalOf<NumberFormatConfig> { error("No NumberFormatConfig value provided") }

@Composable
fun Amount.formattedString(
  config: NumberFormatConfig = LocalNumberFormatConfig.current,
  includeSign: Boolean = false,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
): String = toString(config, includeSign, isPrivacyEnabled)

@Composable
fun WithCompositionLocals(
  isPrivacyEnabled: Boolean = false,
  format: NumberFormat = NumberFormat.Default,
  hideFractions: Boolean = false,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalNumberFormatConfig provides NumberFormatConfig(format, hideFractions),
    LocalPrivacyEnabled provides isPrivacyEnabled,
  ) {
    content()
  }
}
