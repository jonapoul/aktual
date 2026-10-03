package aktual.budget.model

import aktual.budget.model.SyncedPrefKey.PerAccount
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SyncedPrefKeyAccountTest {
  @Test
  fun decoding() {
    assertThat(SyncedPrefKey.decode("csv-delimiter-abc-123"))
      .isEqualTo(PerAccount.CsvDelimiter(AccountId("abc-123")))
  }

  @Test
  fun `Decode bank sync keys`() {
    val id = AccountId("abc-123")
    assertThat(SyncedPrefKey.decode("sync-import-pending-abc-123"))
      .isEqualTo(PerAccount.SyncImportPending(id))
    assertThat(SyncedPrefKey.decode("sync-import-transactions-abc-123"))
      .isEqualTo(PerAccount.SyncImportTransactions(id))
    assertThat(SyncedPrefKey.decode("custom-sync-mappings-abc-123"))
      .isEqualTo(PerAccount.CustomSyncMappings(id))
    assertThat(PerAccount.SyncUpdateDates(id).key).isEqualTo("sync-update-dates-abc-123")
  }
}
