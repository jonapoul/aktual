package aktual.prefs.migration

import aktual.prefs.migration.CurrencyOrdinalToCode.Companion.NewKey
import aktual.prefs.migration.CurrencyOrdinalToCode.Companion.OldKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class CurrencyOrdinalToCodeTest {
  private val migration = CurrencyOrdinalToCode()

  @Test
  fun `Old ordinal maps to the currency it used to mean`() = runTest {
    // 41 was VietnameseDong before ZAR, BDT, NOK and TZS were added
    assertThat(migrate(preferencesOf(OldKey to 41))).isEqualTo(preferencesOf(NewKey to "VND"))
  }

  @Test
  fun `Out of range ordinal is dropped`() = runTest {
    assertThat(migrate(preferencesOf(OldKey to 999))).isEqualTo(emptyPreferences())
  }

  @Test
  fun `Existing code isn't overwritten`() = runTest {
    val prefs = preferencesOf(OldKey to 41, NewKey to "GBP")
    assertThat(migrate(prefs)).isEqualTo(preferencesOf(NewKey to "GBP"))
  }

  @Test
  fun `Skips when old key is missing`() = runTest {
    assertThat(migration.shouldMigrate(preferencesOf(NewKey to "GBP"))).isFalse()
  }

  private suspend fun migrate(prefs: Preferences): Preferences {
    check(migration.shouldMigrate(prefs))
    return migration.migrate(prefs)
  }
}
