package aktual.budget.reports.ui.report

import aktual.budget.model.Condition
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.FormulaQuery
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.TimeFrame
import aktual.core.theme.DarkColors
import aktual.core.ui.PreviewWithColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class QueryScreenTest {
  private var query by mutableStateOf(QUERY)
  private var closed = false
  private val renames = mutableListOf<String>()
  private var deleted = false

  @Test
  fun `Adding a condition opens it in the sheet`() = runComposeUiTest {
    // given
    setScreen()

    // when
    onNodeWithTag(Tags.AddCondition).performClick()

    // then
    waitForIdle()
    assertThat(query.conditions)
      .containsExactly(PAYEE_CONDITION, Condition(Payee, Is, JsonNull, type = Id))
    onNodeWithText("Edit condition").assertExists()
  }

  @Test
  fun `Done closes the sheet and keeps the condition`() = runComposeUiTest {
    // given
    setScreen()
    onNodeWithTag(Tags.ConditionRow).performClick()

    // when
    onNodeWithTag(Tags.QuerySheetDone).performClick()

    // then
    waitForIdle()
    onNodeWithText("Edit condition").assertDoesNotExist()
    assertThat(query.conditions).containsExactly(PAYEE_CONDITION)
  }

  @Test
  fun `Delete removes the condition`() = runComposeUiTest {
    // given
    setScreen()
    onNodeWithTag(Tags.ConditionRow).performClick()

    // when
    onNodeWithTag(Tags.QuerySheetDelete).performClick()

    // then
    waitForIdle()
    assertThat(query.conditions).hasSize(0)
    onAllNodesWithTag(Tags.ConditionRow).assertCountEquals(0)
  }

  @Test
  fun `Conditions the editor can't handle only show a message`() = runComposeUiTest {
    // given
    query = QUERY.copy(conditions = listOf(Condition(Date, Is, JsonObject(emptyMap()))))
    setScreen()

    // when
    onNodeWithTag(Tags.ConditionRow).performClick()

    // then
    onNodeWithText("can't be edited", substring = true).assertExists()
    onNodeWithText("Field").assertDoesNotExist()
  }

  @Test
  fun `Match any switches the operator`() = runComposeUiTest {
    // given
    setScreen()

    // when
    onNodeWithText("Match any").performClick()

    // then
    waitForIdle()
    assertThat(query.conditionsOp).isEqualTo(Or)
  }

  @Test
  fun `Choosing a time frame mode keeps its months`() = runComposeUiTest {
    // given
    setScreen()
    onNodeWithTag(Tags.TimeFrameRow).performClick()

    // when
    onNodeWithText("Static").performClick()

    // then
    waitForIdle()
    assertThat(query.timeFrame).isEqualTo(TimeFrame(MONTH, MONTH, Static))
  }

  @Test
  fun `A query without a time frame starts on the current month`() = runComposeUiTest {
    // given
    query = QUERY.copy(timeFrame = null)
    setScreen()
    onNodeWithTag(Tags.TimeFrameRow).performClick()

    // when
    onNodeWithText("Year to date").performClick()

    // then
    waitForIdle()
    assertThat(query.timeFrame).isEqualTo(TimeFrame(CURRENT, CURRENT, YearToDate))
  }

  @Test
  fun `Back closes the screen`() = runComposeUiTest {
    // given
    setScreen()

    // when
    onNodeWithContentDescription("Navigate back").performClick()

    // then
    assertThat(closed).isTrue()
  }

  @Test
  fun `Nothing is editable until the editor data loads`() = runComposeUiTest {
    // when
    setScreen(data = null)

    // then
    onNodeWithTag(Tags.AddCondition).assertDoesNotExist()
    assertThat(query).all {
      prop(FormulaQuery::conditions).containsExactly(PAYEE_CONDITION)
      prop(FormulaQuery::timeFrame).isEqualTo(QUERY.timeFrame)
    }
  }

  @Test
  fun `Renaming sends the new name`() = runComposeUiTest {
    // given
    setScreen()
    onNodeWithTag(Tags.RenameQuery).performClick()

    // when
    onNode(hasSetTextAction()).performTextReplacement("food")
    onNodeWithText("Rename").performClick()

    // then
    assertThat(renames).containsExactly("food")
  }

  @Test
  fun `Renaming to a taken name is rejected`() = runComposeUiTest {
    // given
    setScreen()
    onNodeWithTag(Tags.RenameQuery).performClick()

    // when
    onNode(hasSetTextAction()).performTextReplacement("income")

    // then
    onNodeWithText("Rename").assertIsNotEnabled()
    assertThat(renames).isEmpty()
  }

  @Test
  fun `Deleting asks first`() = runComposeUiTest {
    // given
    setScreen()

    // when
    onNodeWithTag(Tags.DeleteQuery).performClick()

    // then
    onNodeWithText("Delete groceries?").assertExists()
    assertThat(deleted).isFalse()

    // when
    onNodeWithTag(Tags.ConfirmDeleteQuery).performClick()

    // then
    assertThat(deleted).isTrue()
  }

  @Test
  fun `Cancelling a delete keeps the query`() = runComposeUiTest {
    // given
    setScreen()
    onNodeWithTag(Tags.DeleteQuery).performClick()

    // when
    onNodeWithText("Cancel").performClick()

    // then
    onNodeWithText("Delete groceries?").assertDoesNotExist()
    assertThat(deleted).isFalse()
  }

  private fun ComposeUiTest.setScreen(data: QueryEditorData? = PREVIEW_QUERY_DATA) = setContent {
    PreviewWithColors(DarkColors) {
      QueryScreen(
        name = "groceries",
        query = query,
        data = data,
        isNameFree = { it !in TAKEN },
        onChange = { query = it },
        onRename = { renames += it },
        onDelete = { deleted = true },
        onClose = { closed = true },
      )
    }
  }

  private companion object {
    val TAKEN = setOf("groceries", "income")
    val MONTH = YearMonth(2025, 3)
    val CURRENT = PREVIEW_QUERY_DATA.currentMonth
    val PAYEE_CONDITION = Condition(Payee, Is, JsonPrimitive("employer"), type = Id)
    val QUERY =
      FormulaQuery(
        conditions = listOf(PAYEE_CONDITION),
        conditionsOp = And,
        timeFrame = TimeFrame(MONTH, MONTH, SlidingWindow),
      )
  }
}
