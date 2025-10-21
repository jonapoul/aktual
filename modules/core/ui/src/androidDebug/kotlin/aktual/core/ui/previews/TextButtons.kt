/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui.previews

import aktual.core.ui.BareTextButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.PrimaryTextButtonWithLoading
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun Bare(
  @PreviewParameter(BooleanParameters::class) enabled: Boolean,
) = PreviewThemedColumn {
  BareTextButton(
    text = "Bare",
    isEnabled = enabled,
    onClick = {},
  )
}

@Preview
@Composable
private fun Primary(
  @PreviewParameter(BooleanParameters::class) enabled: Boolean,
) = PreviewThemedColumn {
  PrimaryTextButton(
    text = "Primary",
    isEnabled = enabled,
    onClick = {},
  )
}

@Preview
@Composable
private fun Normal(
  @PreviewParameter(BooleanParameters::class) enabled: Boolean,
) = PreviewThemedColumn {
  NormalTextButton(
    text = "Normal",
    isEnabled = enabled,
    onClick = {},
  )
}

@Preview
@Composable
private fun PrimaryWithLoadingNotLoading(
  @PreviewParameter(BooleanParameters::class) isLoading: Boolean,
) = PreviewThemedColumn {
  PrimaryTextButtonWithLoading(
    text = "OK",
    isLoading = isLoading,
    onClick = {},
  )
}
