package aktual.budget.transactions.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionSet
import aktual.budget.model.TransactionSearch
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive

class TransactionFiltersTest {
  private lateinit var filters: TransactionFilters

  @BeforeTest
  fun before() {
    filters = TransactionFilters()
  }

  @Test
  fun `Query is set`() = runTest {
    filters.test {
      assertThat(awaitItem()).isEqualTo(TransactionSearch())

      // when
      filters.setQuery("argos")

      // then
      assertThat(awaitItem()).isEqualTo(TransactionSearch(text = "argos"))
    }
  }

  @Test
  fun `Conditions are added, edited and removed`() = runTest {
    filters.test {
      skipItems(count = 1)

      // when
      filters.addCondition(PAYEE)
      filters.addCondition(NOTES)

      // then
      assertThat(awaitItem()).isEqualTo(search(PAYEE))
      assertThat(awaitItem()).isEqualTo(search(PAYEE, NOTES))

      // when
      filters.editCondition(index = 0, CATEGORY)

      // then
      assertThat(awaitItem()).isEqualTo(search(CATEGORY, NOTES))

      // when
      filters.removeCondition(index = 1)

      // then
      assertThat(awaitItem()).isEqualTo(search(CATEGORY))
    }
  }

  @Test
  fun `Out of range indices change nothing`() = runTest {
    // given
    filters.addCondition(PAYEE)

    filters.test {
      skipItems(count = 1)

      // when
      filters.editCondition(index = 1, NOTES)
      filters.removeCondition(index = -1)
      filters.setQuery("x")

      // then
      assertThat(awaitItem()).isEqualTo(TransactionSearch("x", ConditionSet(listOf(PAYEE))))
    }
  }

  @Test
  fun `Op is set without touching conditions`() = runTest {
    // given
    filters.addCondition(PAYEE)

    filters.test {
      skipItems(count = 1)

      // when
      filters.setConditionsOp(Or)

      // then
      assertThat(awaitItem())
        .isEqualTo(TransactionSearch(conditions = ConditionSet(listOf(PAYEE), Or)))
    }
  }

  @Test
  fun `Clearing resets everything`() = runTest {
    // given
    filters.setQuery("argos")
    filters.addCondition(PAYEE)
    filters.setConditionsOp(Or)

    filters.test {
      skipItems(count = 1)

      // when
      filters.reset()

      // then
      assertThat(awaitItem()).isEqualTo(TransactionSearch())
    }
  }

  private fun search(vararg conditions: Condition) =
    TransactionSearch(conditions = ConditionSet(conditions.toList()))

  private companion object {
    val PAYEE = Condition(Payee, Is, JsonPrimitive("a"))
    val NOTES = Condition(Notes, Contains, JsonPrimitive("lunch"))
    val CATEGORY = Condition(Category, Is, JsonPrimitive("b"))
  }
}
