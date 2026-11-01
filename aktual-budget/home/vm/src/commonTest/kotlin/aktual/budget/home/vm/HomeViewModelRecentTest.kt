package aktual.budget.home.vm

import aktual.budget.db.dao.TransactionDao
import aktual.budget.home.vm.RecentCardState.Loaded
import aktual.budget.model.TransactionId
import aktual.test.TestCalendar
import aktual.test.runDatabaseTest
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class HomeViewModelRecentTest {
  @Test
  fun `No transactions is empty`() = runDatabaseTest { scope ->
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test { assertThat(awaitRecent()).isEqualTo(Empty) }
  }

  @Test
  fun `Newest transactions come first and are capped`() = runDatabaseTest { scope ->
    val dao = TransactionDao(this)
    for (day in 1..7) dao.insert("t$day", "a", "c", "p", LocalDate(2026, 4, day))
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test {
      assertThat(awaitRecentIds()).containsExactly("t7", "t6", "t5", "t4", "t3")
    }
  }

  @Test
  fun `New transactions show up`() = runDatabaseTest { scope ->
    val dao = TransactionDao(this)
    dao.insert("t1", "a", "c", "p", LocalDate(2026, 4, 1))
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test {
      assertThat(awaitRecentIds()).containsExactly("t1")

      dao.insert("t2", "a", "c", "p", LocalDate(2026, 4, 2))
      var ids = awaitRecentIds()
      while (ids.size < 2) ids = awaitRecentIds()
      assertThat(ids).containsExactly("t2", "t1")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Split parts aren't listed on their own`() = runDatabaseTest { scope ->
    val dao = TransactionDao(this)
    val date = LocalDate(2026, 4, 1)
    dao.insert("parent", "a", null, "p", date, amount = -20.0, isParent = true)
    dao.insert("child1", "a", "c", "p", date, amount = -10.0, parent = "parent")
    dao.insert("child2", "a", "c", "p", date, amount = -10.0, parent = "parent")
    val viewModel = createHomeViewModel(scope, CALENDAR)

    viewModel.state.test { assertThat(awaitRecentIds()).containsExactly("parent") }
  }

  private suspend fun ReceiveTurbine<HomeState>.awaitRecent() = awaitSettled().recent

  private suspend fun ReceiveTurbine<HomeState>.awaitRecentIds() =
    (awaitRecent() as Loaded).transactions.map { it.id }.map(TransactionId::value)

  private companion object {
    val CALENDAR = TestCalendar(LocalDate(2026, 4, 15))
  }
}
