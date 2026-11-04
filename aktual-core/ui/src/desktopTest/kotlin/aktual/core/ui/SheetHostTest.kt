package aktual.core.ui

import aktual.test.setPreviewContent
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test

class SheetHostTest {
  private val events = mutableListOf<String>()
  private var page by mutableStateOf(Page.First)
  private var isShowing by mutableStateOf(true)

  @Test
  fun `Back button on a sub-page returns to the first page`() = runPagesTest {
    // given
    back().assertDoesNotExist()
    click("Open")
    onNodeWithText("Second page").assertExists()

    // when
    back().performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()

    // then
    onNodeWithText("First page").assertExists()
    back().assertDoesNotExist()
    assertThat(events).isEmpty()
  }

  @Test
  fun `Back from a page nested in a sub-page keeps the sub-page's back button`() =
    runComposeUiTest {
      // given
      var isNested by mutableStateOf(true)
      setPreviewContent {
        SheetHost(onDismiss = {}) { host ->
          HostedSheet(onDismiss = { page = First }) {
            if (isNested) {
              HostedSheet(onDismiss = { isNested = false }) { Text("Nested page") }
            } else {
              Text("Second page")
            }
          }
        }
      }
      onNodeWithText("Nested page").assertExists()

      // when
      back().performSemanticsAction(SemanticsActions.OnClick)
      waitForIdle()

      // then
      onNodeWithText("Second page").assertExists()
      back().assertExists()
    }

  @Test
  fun `Closing a sub-page goes back without dismissing the host`() = runPagesTest {
    // given
    click("Open")

    // when
    click("Close")

    // then
    onNodeWithText("First page").assertExists()
    assertThat(events).containsExactly("second closed")
  }

  @Test
  fun `Dismissing from a sub-page hides the whole sheet`() = runPagesTest {
    // given
    click("Open")

    // when
    click("Dismiss")

    // then
    onNodeWithText("Second page").assertDoesNotExist()
    onNodeWithText("First page").assertDoesNotExist()
    assertThat(events).containsExactly("dismissed", "host dismissed")
  }

  @Test
  fun `Closing the first page dismisses the host`() = runPagesTest {
    // when
    click("Close")

    // then
    onNodeWithText("First page").assertDoesNotExist()
    assertThat(events).containsExactly("first closed", "host dismissed")
  }

  @Test
  fun `Without a host the sheet opens its own and has no back button`() = runComposeUiTest {
    // given
    setPreviewContent {
      if (isShowing) {
        HostedSheet(
          onDismiss = {
            events += "dismissed"
            isShowing = false
          },
        ) {
          Text("Lone page")
          TextButton(onClick = { close { events += "closed" } }) { Text("Close") }
        }
      }
    }
    onNodeWithText("Lone page").assertExists()
    back().assertDoesNotExist()

    // when
    click("Close")

    // then
    onNodeWithText("Lone page").assertDoesNotExist()
    assertThat(events).containsExactly("closed", "dismissed")
  }

  private fun runPagesTest(test: ComposeUiTest.() -> Unit) = runComposeUiTest {
    setPreviewContent { if (isShowing) Pages() }
    onNodeWithText("First page").assertExists()
    test()
  }

  @Composable
  private fun Pages() =
    SheetHost(
      onDismiss = {
        events += "host dismissed"
        isShowing = false
      },
    ) { host ->
      when (page) {
        First ->
          HostedSheet(onDismiss = host.dismiss) {
            Text("First page")
            TextButton(onClick = { page = Second }) { Text("Open") }
            TextButton(onClick = { close { events += "first closed" } }) { Text("Close") }
          }

        Second ->
          HostedSheet(onDismiss = { page = First }) {
            Text("Second page")
            TextButton(onClick = { close { events += "second closed" } }) { Text("Close") }
            TextButton(onClick = { dismiss { events += "dismissed" } }) { Text("Dismiss") }
          }
      }
    }

  // A pointer click doesn't reach a sheet's buttons in desktop tests
  private fun ComposeUiTest.click(text: String) {
    onNodeWithText(text).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
  }

  private fun ComposeUiTest.back() = onNodeWithTag(Tags.SheetBack)

  private enum class Page {
    First,
    Second,
  }
}
