/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.test

import aktual.core.model.ColorSchemeType
import aktual.core.ui.LocalColorSchemeType
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog

fun ComposeContentTestRule.printTreeToLog(root: SemanticsNodeInteraction = onRoot()) = root.printToLog("ComposeTree")

fun ComposeContentTestRule.runTest(block: ComposeContentTestRule.() -> Unit) {
  run(block)
}

fun ComposeContentTestRule.onDisplayedNodeWithTag(tag: String) = onNodeWithTag(tag).assertIsDisplayed()

fun ComposeContentTestRule.setThemedContent(
  theme: Theme = Theme.dark(),
  type: ColorSchemeType = ColorSchemeType.Dark,
  content: @Composable () -> Unit,
) = setContent {
  CompositionLocalProvider(
    LocalTheme provides theme,
    LocalColorSchemeType provides type,
    content = content,
  )
}
