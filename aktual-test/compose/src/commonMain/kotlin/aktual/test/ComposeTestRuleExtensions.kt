package aktual.test

import aktual.core.theme.Colors
import aktual.core.theme.DarkColors
import aktual.core.theme.LocalColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog

fun ComposeContentTestRule.printTreeToLog(root: SemanticsNodeInteraction = onRoot()) =
  root.printToLog("ComposeTree")

fun ComposeContentTestRule.runTest(block: ComposeContentTestRule.() -> Unit) {
  run(block)
}

fun ComposeContentTestRule.onDisplayedNodeWithTag(tag: String) =
  onNodeWithTag(tag).assertIsDisplayed()

fun ComposeContentTestRule.setThemedContent(
  colors: Colors = DarkColors,
  content: @Composable () -> Unit,
) = setContent { CompositionLocalProvider(LocalColors provides colors, content = content) }
