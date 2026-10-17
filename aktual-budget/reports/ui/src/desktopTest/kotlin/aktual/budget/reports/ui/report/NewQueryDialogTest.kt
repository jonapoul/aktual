package aktual.budget.reports.ui.report

import aktual.core.theme.DarkColors
import aktual.core.ui.PreviewWithColors
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test
import kotlinx.collections.immutable.persistentSetOf

class NewQueryDialogTest {
  private val created = mutableListOf<String>()

  @Test
  fun `A new name creates the query`() = runComposeUiTest {
    // given
    setDialog()

    // when
    onNode(hasSetTextAction()).performTextReplacement("rent")
    onNodeWithText("Create").performClick()

    // then
    assertThat(created).containsExactly("rent")
  }

  @Test
  fun `A taken name is rejected`() = runComposeUiTest {
    // given
    setDialog()

    // when
    onNode(hasSetTextAction()).performTextReplacement("groceries")

    // then
    onNodeWithText("Create").assertIsNotEnabled()
    assertThat(created).isEmpty()
  }

  private fun ComposeUiTest.setDialog() = setContent {
    PreviewWithColors(DarkColors) {
      NewQueryDialog(
        queries = persistentSetOf("groceries"),
        onConfirm = { created += it },
        onDismiss = {},
      )
    }
  }
}
