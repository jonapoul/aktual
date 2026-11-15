package aktual.budget.banksync.domain

import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import kotlin.test.Test

internal class BankSyncSettingsWriterTest {
  @Test
  fun `Settings are sent as synced preferences`() = runBankSyncTest {
    val settings =
      BankSyncSettings(
        importNotes = false,
        importPending = true,
        importTransactions = false,
        reimportDeleted = false,
        updateDates = true,
        mappings = SyncMappings.Default.with(Deposit, Date, "valueDate"),
      )

    BankSyncSettingsWriter(this).save(ACCOUNT, settings)

    fun pref(key: String, value: String) =
      Change(PREFERENCES, "$key-account-1", "value", string(value))
    assertThat(lastSync())
      .containsExactlyInAnyOrder(
        pref("custom-sync-mappings", settings.mappings.encode()),
        pref("sync-import-pending", "true"),
        pref("sync-import-notes", "false"),
        pref("sync-reimport-deleted", "false"),
        pref("sync-import-transactions", "false"),
        pref("sync-update-dates", "true"),
      )
  }

  @Test
  fun `Saved settings load back`() = runBankSyncTest {
    val settings =
      BankSyncSettings(
        importNotes = false,
        updateDates = true,
        mappings = SyncMappings.Default.with(Payment, Payee, "creditorName"),
      )

    BankSyncSettingsWriter(this).save(ACCOUNT, settings)

    assertThat(BankSyncSettingsLoader(preferences).load(ACCOUNT)).isEqualTo(settings)
  }
}
