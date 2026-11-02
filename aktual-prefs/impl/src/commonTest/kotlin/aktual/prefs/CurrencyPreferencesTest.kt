package aktual.prefs

import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import alakazam.test.unconfinedDispatcher
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class CurrencyPreferencesTest {
  @Test
  fun `Stores currency by code`() = runTest {
    val dataStore = buildPreferences(unconfinedDispatcher)
    val preferences = CurrencyPreferencesImpl(dataStore)
    preferences.currency.set(NorwegianKrone)
    assertThat(dataStore.data.first()[KEY]).isEqualTo("NOK")
  }

  @Test
  fun `Decodes stored code`() = runTest {
    val dataStore = buildPreferences(unconfinedDispatcher)
    val preferences = CurrencyPreferencesImpl(dataStore)
    preferences.currency.asFlow().test {
      awaitItem()
      dataStore.edit { it[KEY] = "ZAR" }
      assertThatNextEmissionIsEqualTo(SouthAfricanRand)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Unrecognised code decodes as unknown`() = runTest {
    val dataStore = buildPreferences(unconfinedDispatcher)
    val preferences = CurrencyPreferencesImpl(dataStore)
    preferences.currency.asFlow().test {
      awaitItem()
      dataStore.edit { it[KEY] = "XYZ" }
      assertThatNextEmissionIsEqualTo(Unknown)
      cancelAndIgnoreRemainingEvents()
    }
  }

  private companion object {
    val KEY = stringPreferencesKey("currencyCode")
  }
}
