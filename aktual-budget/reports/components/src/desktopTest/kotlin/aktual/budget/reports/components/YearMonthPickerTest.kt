package aktual.budget.reports.components

import aktual.test.setPreviewContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

class YearMonthPickerTest {
  @Test
  fun `Tapping the field opens the dialog and saving calls back`() = runComposeUiTest {
    // given
    var saved: YearMonth? = null
    setPreviewContent {
      YearMonthPicker(value = FEBRUARY, range = RANGE, onValueChange = { saved = it })
    }
    onNodeWithText("Select month").assertDoesNotExist()

    // when
    onNodeWithText("February 2025").performClick()
    onNodeWithText("Select month").assertExists()
    onNodeWithText("Save").performClick()

    // then
    assertThat(saved).isEqualTo(FEBRUARY)
    onNodeWithText("Select month").assertDoesNotExist()
  }

  @Test
  fun `Shows the latest value`() = runComposeUiTest {
    // given
    var value by mutableStateOf(FEBRUARY)
    setPreviewContent { YearMonthPicker(value = value, range = RANGE, onValueChange = {}) }

    // when
    value = YearMonth(2025, Month.MAY)

    // then
    onNodeWithText("May 2025").assertExists()
  }

  private companion object {
    val FEBRUARY = YearMonth(2025, Month.FEBRUARY)
    val RANGE = YearMonthRange(YearMonth(2024, Month.JANUARY), YearMonth(2025, Month.JULY))
  }
}
