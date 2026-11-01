package aktual.budget.reports.components

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.reports.vm.NetWorthAccount
import aktual.budget.reports.vm.NetWorthData
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.YearMonth

class NetWorthBreakdownTest {
  @Test
  fun `Accounts top band first, then the totals and change`() {
    val breakdown = netWorthBreakdown(DATA, FEB)

    assertThat(breakdown).all {
      prop(NetWorthBreakdown::rows)
        .extracting(NetWorthBreakdownRow::name, NetWorthBreakdownRow::index)
        .containsExactly("savings" to 2, "checking" to 1, "debt" to 0)
      prop(NetWorthBreakdown::assets).isEqualTo(Amount(40.0))
      prop(NetWorthBreakdown::debts).isEqualTo(Amount(-3.0))
      prop(NetWorthBreakdown::total).isEqualTo(Amount(37.0))
      prop(NetWorthBreakdown::change).isEqualTo(Amount(12.0))
    }
  }

  @Test
  fun `First month has no change`() {
    assertThat(netWorthBreakdown(DATA, JAN)).prop(NetWorthBreakdown::change).isNull()
  }

  @Test
  fun `Trend mode only shows the totals`() {
    val breakdown = netWorthBreakdown(DATA.copy(mode = Trend), FEB)

    assertThat(breakdown).all {
      prop(NetWorthBreakdown::rows).isEmpty()
      prop(NetWorthBreakdown::assets).isEqualTo(Amount(40.0))
      prop(NetWorthBreakdown::debts).isEqualTo(Amount(-3.0))
    }
  }

  private companion object {
    val JAN = YearMonth(2026, 1)
    val FEB = YearMonth(2026, 2)

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

    fun account(id: String, jan: Double, feb: Double) =
      NetWorthAccount(
        id = AccountId(id),
        name = id,
        balances = persistentMapOf(JAN to Amount(jan), FEB to Amount(feb)),
      )
  }
}
