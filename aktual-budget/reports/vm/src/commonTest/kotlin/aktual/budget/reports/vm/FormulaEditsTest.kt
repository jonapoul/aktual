package aktual.budget.reports.vm

import aktual.budget.model.Condition
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonNull

class FormulaEditsTest {
  @Test
  fun `New queries match everything in the current month, moving with time`() {
    // when
    val edits = EDITS.withNewQuery("new", MONTH)

    // then
    assertThat(edits.queries.keys.toList()).containsExactly("a", "b", "new")
    assertThat(edits.queries.getValue("new"))
      .isEqualTo(FormulaQuery(emptyList(), And, TimeFrame(MONTH, MONTH, SlidingWindow)))
  }

  @Test
  fun `Changing a query keeps its place`() {
    // given
    val changed = QUERY.copy(conditionsOp = Or)

    // when
    val edits = EDITS.withQuery("a", changed)

    // then
    assertThat(edits.queries.keys.toList()).containsExactly("a", "b")
    assertThat(edits.queries.getValue("a")).isEqualTo(changed)
  }

  @Test
  fun `Renaming a query keeps its place`() {
    // when
    val edits = EDITS.withQueryRenamed("a", "z")

    // then
    assertThat(edits.queries.keys.toList()).containsExactly("z", "b")
    assertThat(edits.queries.getValue("z")).isEqualTo(QUERY)
  }

  @Test
  fun `Removing a query leaves the others`() {
    // when
    val edits = EDITS.withoutQuery("a")

    // then
    assertThat(edits.queries.keys.toList()).containsExactly("b")
  }

  private companion object {
    val MONTH = YearMonth(2025, 6)
    val QUERY =
      FormulaQuery(listOf(Condition(Payee, Is, JsonNull)), conditionsOp = And, timeFrame = null)
    val EDITS =
      FormulaReportMeta(queries = mapOf("a" to QUERY, "b" to QUERY.copy(conditions = emptyList())))
        .toEdits()
  }
}
