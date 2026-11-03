package aktual.budget.home.vm

import aktual.budget.home.vm.HomeCardType.Accounts
import aktual.budget.home.vm.HomeCardType.Attention
import aktual.budget.home.vm.HomeCardType.Recent
import aktual.budget.home.vm.HomeCardType.Reports
import aktual.budget.home.vm.HomeCardType.ThisMonth
import aktual.budget.home.vm.HomeCardType.Upcoming
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

internal class HomeCardTypeTest {
  @Test
  fun `No saved order gives the default`() {
    assertThat(homeCardOrder(saved = null))
      .containsExactly(ThisMonth, Attention, Upcoming, Recent, Accounts, Reports)
  }

  @Test
  fun `Saved order is kept`() {
    assertThat(homeCardOrder(["Reports", "Accounts", "Upcoming", "Attention", "ThisMonth"]))
      .containsExactly(Reports, Accounts, Upcoming, Attention, ThisMonth, Recent)
  }

  @Test
  fun `Unknown and repeated names are dropped and missing cards go at the end`() {
    assertThat(homeCardOrder(["Upcoming", "Removed", "Upcoming", "Accounts"]))
      .containsExactly(Upcoming, Accounts, ThisMonth, Attention, Recent, Reports)
  }

  @Test
  fun `Swapping leaves the cards between in place`() {
    assertThat([ThisMonth, Attention, Upcoming, Accounts].swapping(Accounts, Attention))
      .containsExactly(ThisMonth, Accounts, Upcoming, Attention)
  }

  @Test
  fun `Reordering some cards leaves the others in place`() {
    assertThat(
        [ThisMonth, Accounts, Attention, Upcoming].reordering([Upcoming, ThisMonth, Attention]),
      )
      .containsExactly(Upcoming, Accounts, ThisMonth, Attention)
  }
}
