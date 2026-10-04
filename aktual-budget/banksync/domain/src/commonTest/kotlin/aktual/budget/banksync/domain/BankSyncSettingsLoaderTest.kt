package aktual.budget.banksync.domain

import aktual.budget.model.SyncedPrefKey.PerAccount.CustomSyncMappings
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportNotes
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportPending
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncUpdateDates
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

internal class BankSyncSettingsLoaderTest {
  @Test
  fun `Unset preferences take their defaults`() = runBankSyncTest {
    assertThat(BankSyncSettingsLoader(preferences).load(ACCOUNT)).isEqualTo(BankSyncSettings())
  }

  @Test
  fun `Stored preferences are read`() = runBankSyncTest {
    preferences[SyncImportNotes(ACCOUNT)] = "false"
    preferences[SyncImportPending(ACCOUNT)] = "nonsense"
    preferences[SyncUpdateDates(ACCOUNT)] = "true"
    preferences[CustomSyncMappings(ACCOUNT)] = ""

    assertThat(BankSyncSettingsLoader(preferences).load(ACCOUNT))
      .isEqualTo(BankSyncSettings(importNotes = false, importPending = false, updateDates = true))
  }
}
