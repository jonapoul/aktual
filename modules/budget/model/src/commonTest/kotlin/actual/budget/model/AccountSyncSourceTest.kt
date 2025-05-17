package actual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import kotlin.test.Test
import kotlin.test.assertEquals

@Burst
@Suppress("JUnitMalformedDeclaration")
class AccountSyncSourceTest(
  val source: AccountSyncSource = burstValues(
    AccountSyncSource.SimpleFin,
    AccountSyncSource.GoCardless,
    AccountSyncSource.PluggyAi,
    AccountSyncSource.Other("something-else"),
  ),
) {
  @Test
  fun `Encode and decode`() {
    val encoded = source.toString()
    val decoded = AccountSyncSource.fromString(encoded)
    assertEquals(decoded, source)
  }
}
