/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.core.ui

import actual.budget.model.Amount
import actual.budget.model.NumberFormat
import actual.budget.model.NumberFormatConfig
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
