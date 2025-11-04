/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
