/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui

import aktual.core.model.ColorSchemeType
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun AktualTheme(
  type: ColorSchemeType,
  content: @Composable () -> Unit,
) {
  val theme = remember(type) {
    when (type) {
      ColorSchemeType.Light -> LightTheme()
      ColorSchemeType.Dark -> DarkTheme()
      ColorSchemeType.Midnight -> MidnightTheme()
    }
  }

  CompositionLocalProvider(
    LocalTheme provides theme,
    LocalColorSchemeType provides type,
    LocalIndication provides ripple(),
  ) {
    SetStatusBarColors(
      theme = theme,
    )

    val materialColorScheme = when (theme) {
      is LightTheme -> lightColorScheme()
      is DarkTheme -> darkColorScheme()
      is MidnightTheme -> darkColorScheme(surface = Color.Black)
    }

    MaterialTheme(
      colorScheme = materialColorScheme,
      typography = aktualTypography(theme),
      content = content,
    )
  }
}
