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
package aktual.core.ui.previews

import aktual.core.model.ColorSchemeType
import aktual.core.ui.SingleScreenPreview
import aktual.core.ui.WavyBackground
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun BackgroundImage(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = WavyBackground(
  schemeType = type,
)

@SingleScreenPreview
@Composable
private fun PreviewScaled(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = WavyBackground(
  schemeType = type,
)
