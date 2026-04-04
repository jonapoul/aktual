package aktual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

@Burst
class AccountSyncSourceTest {
  @Test
  fun `Encode and decode`(
    source: AccountSyncSource =
      burstValues(
        AccountSyncSource.SimpleFin,
        AccountSyncSource.GoCardless,
        AccountSyncSource.PluggyAi,
        AccountSyncSource.Other("something-else"),
      )
  ) {
    val encoded = source.toString()
    assertThat(source).isEqualTo(AccountSyncSource.fromString(encoded))
  }
}
