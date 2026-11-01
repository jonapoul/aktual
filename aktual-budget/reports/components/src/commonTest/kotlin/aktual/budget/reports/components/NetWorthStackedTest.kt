package aktual.budget.reports.components

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.reports.vm.NetWorthAccount
import aktual.budget.reports.vm.NetWorthData
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.YearMonth

class NetWorthStackedTest {
  @Test
  fun `Each line is the running total of the accounts before it`() {
    val stacks = netWorthStacks(DATA)

    assertThat(stacks)
      .containsExactly(
        listOf(-5.0, -3.0),
        listOf(5.0, 7.0),
        listOf(25.0, 37.0),
      )
  }

  @Test
  fun `Last line is net worth`() {
    val last = netWorthStacks(DATA).last()

    assertThat(last).containsExactly(*DATA.items.values.map { it.toDouble() }.toTypedArray())
  }

  @Test
  fun `Trend mode or no accounts isn't stacked`() {
    assertThat(DATA.isStacked()).isTrue()
    assertThat(DATA.copy(mode = Trend).isStacked()).isFalse()
    assertThat(DATA.copy(accounts = persistentListOf()).isStacked()).isFalse()
    assertThat(netWorthStacks(DATA.copy(accounts = persistentListOf()))).isEmpty()
  }

  private companion object {
    val JAN = YearMonth(2026, 1)
    val FEB = YearMonth(2026, 2)

    fun account(id: String, jan: Double, feb: Double) =
      NetWorthAccount(
        id = AccountId(id),
        name = id,
        balances = persistentMapOf(JAN to Amount(jan), FEB to Amount(feb)),
      )

    val DATA =
      NetWorthData(
        title = null,
        items = persistentMapOf(JAN to Amount(25.0), FEB to Amount(37.0)),
        mode = Stacked,
        accounts =
          persistentListOf(
            account("debt", jan = -5.0, feb = -3.0),
            account("checking", jan = 10.0, feb = 10.0),
            account("savings", jan = 20.0, feb = 30.0),
          ),
      )
  }
}
