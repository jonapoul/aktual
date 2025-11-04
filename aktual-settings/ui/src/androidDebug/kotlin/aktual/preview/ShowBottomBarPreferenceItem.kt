/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.core.ui.PreviewThemedColumn
import aktual.settings.ui.items.ShowBottomBarPreferenceItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewChecked() = PreviewThemedColumn {
  ShowBottomBarPreferenceItem(
    value = true,
    onChange = {},
  )
}

@Preview
@Composable
private fun PreviewUnchecked() = PreviewThemedColumn {
  ShowBottomBarPreferenceItem(
    value = false,
    onChange = {},
  )
}
