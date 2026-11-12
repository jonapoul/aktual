package aktual.budget.reports.vm

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlinx.collections.immutable.persistentMapOf

class QueryConditionsTest {
  @Test
  fun `Renaming keeps the order`() {
    val query = FormulaQuery(emptyList(), conditionsOp = null, timeFrame = null)
    val queries = persistentMapOf("a" to query, "b" to query, "c" to query)
    assertThat(queries.renamed("b", "z").keys.toList()).containsExactly("a", "z", "c")
  }
}
