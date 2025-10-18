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
package actual.core.ui.previews

import actual.core.model.ColorSchemeType
import actual.core.ui.PreviewWithColorScheme
import actual.core.ui.actualTypography
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentMapOf

@Preview(widthDp = 1300)
@Composable
private fun PreviewActual() = PreviewWithColorScheme(ColorSchemeType.Light) {
  PreviewContent(actualTypography())
}

@Preview(widthDp = 1300)
@Composable
private fun PreviewMaterial() = PreviewContent(
  typography = MaterialTheme.typography,
)

private fun Typography.styles() = persistentMapOf(
  "displayLarge" to displayLarge,
  "displayMedium" to displayMedium,
  "displaySmall" to displaySmall,
  "headlineLarge" to headlineLarge,
  "headlineMedium" to headlineMedium,
  "headlineSmall" to headlineSmall,
  "titleLarge" to titleLarge,
  "titleMedium" to titleMedium,
  "titleSmall" to titleSmall,
  "bodyLarge" to bodyLarge,
  "bodyMedium" to bodyMedium,
  "bodySmall" to bodySmall,
  "labelLarge" to labelLarge,
  "labelMedium" to labelMedium,
  "labelSmall" to labelSmall,
)

@Composable
private fun PreviewContent(
  typography: Typography,
  modifier: Modifier = Modifier,
) = Column(
  modifier = modifier.background(Color.White),
) {
  for ((name, style) in typography.styles()) {
    Row(
      modifier = Modifier.height(IntrinsicSize.Min),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .background(Color.LightGray)
          .fillMaxHeight()
          .width(150.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = name,
          textAlign = TextAlign.Start,
        )
      }

      Text(
        modifier = Modifier.weight(1f),
        text = "Quick brown fox? Jumped over the lazy dog!",
        style = style,
      )
    }
  }
}
