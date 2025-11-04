/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.list.ui.ContentFailure
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Failure() = PreviewThemedScreen {
  ContentFailure(
    reason = "Failed to do the thing, here's a bit more text to show how it behaves when wrapping",
    onClickRetry = {},
  )
}

@TripleScreenPreview
@Composable
private fun NoReason() = PreviewThemedScreen {
  ContentFailure(
    reason = null,
    onClickRetry = {},
  )
}
