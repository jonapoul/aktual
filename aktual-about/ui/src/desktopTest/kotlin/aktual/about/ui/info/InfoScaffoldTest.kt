package aktual.about.ui.info

import aktual.about.vm.BuildState
import aktual.core.model.AktualVersions
import aktual.test.setPreviewContent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

class InfoScaffoldTest {
  private val actions = mutableListOf<InfoAction>()

  @Test
  fun `Clicking the app version opens its source`() = runComposeUiTest {
    // given
    setScaffold()

    // when
    versionButton("1.2.3").assertHasClickAction().performClick()

    // then
    assertThat(actions).containsExactly(OpenUrl(APP_URL))
  }

  @Test
  fun `Clicking the server version opens its source`() = runComposeUiTest {
    // given
    setScaffold()

    // when
    versionButton("2.3.4").assertHasClickAction().performClick()

    // then
    assertThat(actions).containsExactly(OpenUrl(SERVER_URL))
  }

  @Test
  fun `Source code button is gone`() = runComposeUiTest {
    // when
    setScaffold()

    // then
    onNodeWithText("Source Code").assertDoesNotExist()
  }

  private fun ComposeUiTest.versionButton(version: String) =
    onNode(
      SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button) and hasText(version),
      useUnmergedTree = false,
    )

  private fun ComposeUiTest.setScaffold() = setPreviewContent {
    InfoScaffold(buildState = BUILD_STATE, onAction = { actions += it })
  }

  private companion object {
    const val APP_URL = "https://github.com/jonapoul/aktual/tree/v1.2.3"
    const val SERVER_URL = "https://github.com/actualbudget/actual/tree/v2.3.4"

    val BUILD_STATE =
      BuildState(
        versions = AktualVersions(app = "1.2.3", server = "2.3.4"),
        buildDate = "12:34 GMT, 1st Feb 2026",
        year = 2026,
        appSourceUrl = APP_URL,
        serverSourceUrl = SERVER_URL,
      )
  }
}
