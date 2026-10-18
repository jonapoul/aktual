package aktual.budget.home.vm

import aktual.budget.home.vm.HomeCardType.Accounts
import aktual.budget.home.vm.HomeCardType.Attention
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
      .containsExactly(ThisMonth, Attention, Upcoming, Accounts, Reports)
  }

  @Test
  fun `Saved order is kept`() {
    assertThat(homeCardOrder(listOf("Reports", "Accounts", "Upcoming", "Attention", "ThisMonth")))
      .containsExactly(Reports, Accounts, Upcoming, Attention, ThisMonth)
  }

  @Test
  fun `Unknown and repeated names are dropped and missing cards go at the end`() {
    assertThat(homeCardOrder(listOf("Upcoming", "Removed", "Upcoming", "Accounts")))
      .containsExactly(Upcoming, Accounts, ThisMonth, Attention, Reports)
  }

  @Test
  fun `Swapping leaves the cards between in place`() {
    assertThat(listOf(ThisMonth, Attention, Upcoming, Accounts).swapping(Accounts, Attention))
      .containsExactly(ThisMonth, Accounts, Upcoming, Attention)
  }

  @Test
  fun `Reordering some cards leaves the others in place`() {
    assertThat(
        listOf(ThisMonth, Accounts, Attention, Upcoming)
          .reordering(listOf(Upcoming, ThisMonth, Attention)),
      )
      .containsExactly(Upcoming, Accounts, ThisMonth, Attention)
  }
}
