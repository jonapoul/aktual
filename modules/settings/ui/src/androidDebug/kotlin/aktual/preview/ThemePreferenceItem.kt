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
package aktual.preview

import aktual.core.model.ColorSchemeType
import aktual.core.model.DarkColorSchemeType
import aktual.core.model.RegularColorSchemeType
import aktual.core.ui.PreviewWithColorScheme
import aktual.settings.ui.items.ThemePreferenceItem
import aktual.settings.vm.ThemeConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewSystem() = PreviewThemed(RegularColorSchemeType.System)

@Preview
@Composable
private fun PreviewLight() = PreviewThemed(RegularColorSchemeType.Light)

@Preview
@Composable
private fun PreviewDark() = PreviewThemed(RegularColorSchemeType.Dark, dark = DarkColorSchemeType.Dark)

@Preview
@Composable
private fun PreviewMidnight() = PreviewThemed(RegularColorSchemeType.Dark, dark = DarkColorSchemeType.Midnight)

@Composable
private fun PreviewThemed(
  regular: RegularColorSchemeType,
  modifier: Modifier = Modifier,
  dark: DarkColorSchemeType = DarkColorSchemeType.Dark,
) {
  var config by remember { mutableStateOf(ThemeConfig(regular, dark)) }
  val schemeType = when (config.regular) {
    RegularColorSchemeType.System -> ColorSchemeType.Light
    RegularColorSchemeType.Light -> ColorSchemeType.Light
    RegularColorSchemeType.Dark -> when (dark) {
      DarkColorSchemeType.Dark -> ColorSchemeType.Dark
      DarkColorSchemeType.Midnight -> ColorSchemeType.Midnight
    }
  }
  PreviewWithColorScheme(
    schemeType = schemeType,
    modifier = modifier,
  ) {
    ThemePreferenceItem(
      config = ThemeConfig(regular, dark),
      onChange = { newValue -> config = newValue },
    )
  }
}
