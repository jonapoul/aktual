/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
