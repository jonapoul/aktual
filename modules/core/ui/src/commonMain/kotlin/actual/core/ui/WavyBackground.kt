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

import actual.core.model.ColorSchemeType
import actual.l10n.Drawables
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

val LocalColorSchemeType = compositionLocalOf<ColorSchemeType> { error("No ColorSchemeType defined!") }

@Composable
fun WavyBackground(
  modifier: Modifier = Modifier,
  schemeType: ColorSchemeType = LocalColorSchemeType.current,
) = Image(
  modifier = modifier.fillMaxSize(),
  painter = backgroundImage(schemeType),
  contentDescription = null,
  contentScale = ContentScale.FillHeight,
)

@Composable
private fun backgroundImage(type: ColorSchemeType) = when (type) {
  ColorSchemeType.Light -> Drawables.wavyBackgroundLight
  ColorSchemeType.Dark -> Drawables.wavyBackgroundDark
  ColorSchemeType.Midnight -> Drawables.wavyBackgroundMidnight
}
