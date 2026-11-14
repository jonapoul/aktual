package aktual.budget.db.dao

import aktual.budget.db.Transaction_filters
import aktual.budget.model.Condition
import aktual.budget.model.TransactionFilterId
import aktual.test.runDatabaseTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlinx.serialization.json.JsonPrimitive

internal class TransactionFilterDaoTest {
  @Test
  fun `Live filters are listed by name, ignoring case`() = runDatabaseTest {
    // given
    val dao = TransactionFilterDao(this)
    dao.insert(filter("b", "beta"))
    dao.insert(filter("c", "Charlie"))
    dao.insert(filter("a", "alpha"))
    dao.insert(filter("d", "Aardvark", tombstone = true))

    // then
    assertThat(dao.getAllActive().map { filter -> filter.name })
      .containsExactly("alpha", "beta", "Charlie")
  }

  @Test
  fun `Observing re-emits when a filter is deleted`() = runDatabaseTest {
    // given
    val dao = TransactionFilterDao(this)
    dao.insert(filter("a", "alpha"))

    dao.observeAllActive().test {
      assertThat(awaitItem().map { filter -> filter.name }).containsExactly("alpha")

      // when
      dao.insert(filter("a", "alpha", tombstone = true))

      // then
      assertThat(awaitItem()).containsExactly()
    }
  }

  private fun filter(id: String, name: String, tombstone: Boolean = false) =
    Transaction_filters(
      id = TransactionFilterId(id),
      name = name,
      conditions = listOf(Condition(Payee, Is, JsonPrimitive("p"))),
      conditions_op = And,
      tombstone = tombstone,
    )
}
