package aktual.budget.home.vm

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

internal class HomeCardTypeTest {
  @Test
  fun `No saved order gives the default`() {
    assertThat(homeCardOrder(saved = null))
      .containsExactly(
        HomeCardType.ThisMonth,
        HomeCardType.Attention,
        HomeCardType.Upcoming,
        HomeCardType.Accounts,
      )
  }

  @Test
  fun `Saved order is kept`() {
    assertThat(homeCardOrder(listOf("Accounts", "Upcoming", "Attention", "ThisMonth")))
      .containsExactly(
        HomeCardType.Accounts,
        HomeCardType.Upcoming,
        HomeCardType.Attention,
        HomeCardType.ThisMonth,
      )
  }

  @Test
  fun `Unknown and repeated names are dropped and missing cards go at the end`() {
    assertThat(homeCardOrder(listOf("Upcoming", "Removed", "Upcoming", "Accounts")))
      .containsExactly(
        HomeCardType.Upcoming,
        HomeCardType.Accounts,
        HomeCardType.ThisMonth,
        HomeCardType.Attention,
      )
  }

  @Test
  fun `Swapping leaves the cards between in place`() {
    assertThat(
        listOf(
            HomeCardType.ThisMonth,
            HomeCardType.Attention,
            HomeCardType.Upcoming,
            HomeCardType.Accounts,
          )
          .swapping(Accounts, Attention),
      )
      .containsExactly(
        HomeCardType.ThisMonth,
        HomeCardType.Accounts,
        HomeCardType.Upcoming,
        HomeCardType.Attention,
      )
  }
}
