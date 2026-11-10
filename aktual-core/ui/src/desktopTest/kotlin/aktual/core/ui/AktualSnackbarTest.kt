package aktual.core.ui

import aktual.test.setPreviewContent
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.SnackbarResult.Dismissed
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class AktualSnackbarTest {
  private var result: SnackbarResult? = null

  @Test
  fun `Clicking a severity snackbar's action performs it`() = runComposeUiTest {
    // given
    show { showErrorSnackbar("Deleted", actionLabel = "Undo") }

    // when
    onNodeWithText("Undo").performClick()
    waitForIdle()

    // then
    assertThat(result).isEqualTo(ActionPerformed)
  }

  @Test
  fun `Clicking a severity snackbar's dismiss action dismisses it`() = runComposeUiTest {
    // given
    show { showSuccessSnackbar("Synced", withDismissAction = true) }

    // when
    onNodeWithContentDescription("Close").performClick()
    waitForIdle()

    // then
    assertThat(result).isEqualTo(Dismissed)
    onNodeWithText("Synced").assertDoesNotExist()
  }

  private fun ComposeUiTest.show(block: suspend SnackbarHostState.() -> SnackbarResult) {
    val host = SnackbarHostState()
    setPreviewContent {
      LaunchedEffect(Unit) { result = host.block() }
      SnackbarHost(host, snackbar = ::AktualSnackbar)
    }
    waitForIdle()
  }
}
