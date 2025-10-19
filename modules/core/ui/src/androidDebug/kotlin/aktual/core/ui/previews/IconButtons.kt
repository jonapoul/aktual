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
import aktual.core.ui.BareIconButton
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.PrimaryIconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun Bare(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  BareIconButton(
    imageVector = Icons.Filled.Check,
    contentDescription = "Cancel",
    onClick = {},
  )
}

@Preview
@Composable
private fun Normal(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  NormalIconButton(
    imageVector = Icons.Filled.Check,
    contentDescription = "Cancel",
    onClick = {},
  )
}

@Preview
@Composable
private fun Primary(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  PrimaryIconButton(
    imageVector = Icons.Filled.Check,
    contentDescription = "OK",
    onClick = {},
  )
}
