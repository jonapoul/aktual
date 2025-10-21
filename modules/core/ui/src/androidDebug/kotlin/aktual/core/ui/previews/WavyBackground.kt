/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
