package aktual.core.ui

import aktual.test.setPreviewContent
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEmpty
import kotlin.test.Test

class AnimatedFlowRowTest {
  private val items = mutableStateListOf<String>()
  private val clicks = mutableListOf<String>()

  @Test
  fun `Items with equal keys are all shown`() = runComposeUiTest {
    // given
    items += listOf("a", "a", "b")

    // when
    setPreviewContent { Chips() }

    // then
    onAllNodesWithText("a").assertCountEquals(2)
    onNodeWithText("b").assertExists()
  }

  @Test
  fun `A removed item ignores taps while it animates out`() = runComposeUiTest {
    // given
    items += listOf("a", "b")
    setPreviewContent { Chips() }
    val bounds = onNodeWithText("b").getBoundsInRoot()
    mainClock.autoAdvance = false

    // when
    items.remove("b")
    mainClock.advanceTimeByFrame()
    mainClock.advanceTimeByFrame()
    onRoot().performTouchInput {
      val x = ((bounds.left + bounds.right) / 2).toPx()
      val y = ((bounds.top + bounds.bottom) / 2).toPx()
      click(Offset(x, y))
    }

    // then
    onNodeWithText("b").assertDoesNotExist()
    assertThat(clicks).isEmpty()
  }

  @Composable
  private fun Chips() = AnimatedFlowRow {
    items.forEach { text ->
      item(key = text) {
        Text(text = text, modifier = Modifier.clickable { clicks += text })
      }
    }
  }
}
