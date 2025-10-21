/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.app.nav

import aktual.core.model.PingState
import aktual.core.ui.BottomBarState
import aktual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewConnected() = PreviewThemedColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      pingState = PingState.Success,
      budgetName = "My Budget",
    ),
  )
}

@Preview
@Composable
private fun PreviewDisconnected() = PreviewThemedColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      pingState = PingState.Failure,
      budgetName = null,
    ),
  )
}

@Preview
@Composable
private fun PreviewUnknown() = PreviewThemedColumn {
  BottomStatusBar(
    state = BottomBarState.Visible(
      pingState = PingState.Unknown,
      budgetName = null,
    ),
  )
}
