package aktual.budget.model

import app.cash.burst.Burst
import app.cash.burst.burstValues
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

@Burst
class BankSyncStatusTest {
  @Test
  fun `Encode and decode`(
    status: BankSyncStatus =
      burstValues(
        BankSyncStatus.Ok,
        BankSyncStatus.Pending,
        BankSyncStatus.SyncRequested,
        BankSyncStatus.Failed,
        BankSyncStatus.ReauthRequired,
        BankSyncStatus.AttentionRequired,
        BankSyncStatus.RateLimitExceeded,
        BankSyncStatus.TimedOut,
        BankSyncStatus.AccountMissing,
        BankSyncStatus.Other("something-else"),
      )
  ) {
    val encoded = status.toString()
    assertThat(status).isEqualTo(BankSyncStatus.fromString(encoded))
  }
}
