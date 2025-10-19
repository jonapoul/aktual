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
