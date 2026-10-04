package aktual.budget.model

import aktual.budget.model.SyncedPrefKey.Companion.decode
import aktual.budget.model.SyncedPrefKey.PerAccount.CsvDelimiter
import aktual.budget.model.SyncedPrefKey.PerAccount.CustomSyncMappings
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportPending
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportTransactions
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncUpdateDates
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SyncedPrefKeyAccountTest {
  @Test
  fun decoding() {
    assertThat(decode("csv-delimiter-abc-123")).isEqualTo(CsvDelimiter(AccountId("abc-123")))
  }

  @Test
  fun `Decode bank sync keys`() {
    val id = AccountId("abc-123")
    assertThat(decode("sync-import-pending-abc-123")).isEqualTo(SyncImportPending(id))
    assertThat(decode("sync-import-transactions-abc-123")).isEqualTo(SyncImportTransactions(id))
    assertThat(decode("custom-sync-mappings-abc-123")).isEqualTo(CustomSyncMappings(id))
    assertThat(SyncUpdateDates(id).key).isEqualTo("sync-update-dates-abc-123")
  }
}
