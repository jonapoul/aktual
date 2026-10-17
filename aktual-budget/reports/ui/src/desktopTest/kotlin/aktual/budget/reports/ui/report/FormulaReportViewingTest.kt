package aktual.budget.reports.ui.report

import aktual.budget.model.Condition
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.vm.FormulaQuery
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.TimeFrame
import aktual.budget.reports.vm.report.FormulaReportState
import aktual.budget.reports.vm.toEdits
import aktual.core.theme.DarkColors
import aktual.core.ui.PreviewWithColors
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class FormulaReportViewingTest {
  @Test
  fun `Empty card shows defaults`() = runComposeUiTest {
    // when
    setViewer(FormulaReportMeta())

    // then
    onNodeWithText("QUERIES · 0").assertExists()
    onNodeWithText("No queries yet").assertExists()
    onNodeWithText("Dynamic").assertExists()
    onNodeWithText("On").assertExists()
  }

  @Test
  fun `Static font size shows the size`() = runComposeUiTest {
    // when
    setViewer(FormulaReportMeta(fontSizeMode = Static, staticFontSize = 24.0, showTitle = false))

    // then
    onNodeWithText("Static · 24").assertExists()
    onNodeWithText("Off").assertExists()
  }

  @Test
  fun `Conditions past the first two are counted`() = runComposeUiTest {
    // when
    setViewer(meta(query(conditions = FOUR_CONDITIONS)))

    // then
    onNodeWithText("QUERIES · 1").assertExists()
    onNodeWithText("Payee is Employer · Category one of Food, Bills · +2").assertExists()
  }

  @Test
  fun `Query without a time frame shows all time`() = runComposeUiTest {
    // when
    setViewer(meta(query(timeFrame = null)))

    // then
    onNodeWithText("All time").assertExists()
  }

  @Test
  fun `Static time frame shows its months`() = runComposeUiTest {
    // when
    setViewer(meta(query(timeFrame = TimeFrame(YearMonth(2025, 1), YearMonth(2025, 6), Static))))

    // then
    onNodeWithText("Jan 2025 - Jun 2025").assertExists()
  }

  @Test
  fun `Conditions summary waits for entity names`() = runComposeUiTest {
    // when
    setViewer(meta(query(conditions = FOUR_CONDITIONS)), data = null)

    // then
    onNodeWithText("Payee", substring = true).assertDoesNotExist()
    onNodeWithText("All time").assertExists()
  }

  @Test
  fun `Condition with an unexpected value shows it raw`() = runComposeUiTest {
    // when
    val condition = Condition(Amount, LessThan, JsonPrimitive("abc"))
    setViewer(meta(query(conditions = listOf(condition))))

    // then
    onNodeWithText("Amount is less than \"abc\"").assertExists()
  }

  @Test
  fun `Rows don't open editors while viewing`() = runComposeUiTest {
    // given
    val actions = mutableListOf<FormulaReportAction>()
    setViewer(FormulaReportMeta(formula = "=1"), onAction = { actions += it })

    // when
    onNodeWithText("=1").performClick()
    onNodeWithText("Font size").performClick()
    onNodeWithTag(Tags.ShowTitle).performClick()

    // then
    assertThat(actions).isEmpty()
    onNodeWithText("Name").assertDoesNotExist()
  }

  @Test
  fun `Edit button starts editing`() = runComposeUiTest {
    // given
    val actions = mutableListOf<FormulaReportAction>()
    setViewer(FormulaReportMeta(), onAction = { actions += it })
    onNodeWithTag(Tags.SaveReport).assertDoesNotExist()

    // when
    onNodeWithTag(Tags.EditReport).performClick()

    // then
    assertThat(actions).containsExactly(FormulaReportAction.StartEditing)
  }

  private fun ComposeUiTest.setViewer(
    meta: FormulaReportMeta,
    data: QueryEditorData? = PREVIEW_QUERY_DATA,
    onAction: (FormulaReportAction) -> Unit = {},
  ) = setContent {
    PreviewWithColors(DarkColors) {
      FormulaReportScaffold(
        state =
          FormulaReportState.Loaded(
            draft = meta.toEdits(),
            preview = PREVIEW_FORMULA_DATA,
            isEditing = false,
            hasChanges = false,
          ),
        queryEditorData = data,
        onAction = onAction,
      )
    }
  }

  private companion object {
    val FOUR_CONDITIONS =
      listOf(
        Condition(Payee, Is, JsonPrimitive("employer")),
        Condition(
          Category,
          OneOf,
          JsonArray(listOf(JsonPrimitive("food"), JsonPrimitive("bills"))),
        ),
        Condition(Notes, Contains, JsonPrimitive("rent")),
        Condition(Cleared, Is, JsonPrimitive(true)),
      )

    fun query(
      conditions: List<Condition> = emptyList(),
      timeFrame: TimeFrame? = null,
    ) = FormulaQuery(conditions = conditions, conditionsOp = And, timeFrame = timeFrame)

    fun meta(query: FormulaQuery) = FormulaReportMeta(queries = mapOf("groceries" to query))
  }
}
