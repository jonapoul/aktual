package aktual.prefs.budget

import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.Currency
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncedPrefKey
import aktual.test.TestBudgetSync
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher

class BudgetFormatPreferencesImplTest {
  @Test
  fun `Unset prefs fall back to upstream's defaults`() = runPrefsTest { prefs, _, _ ->
    assertThat(prefs.currency.get()).isEqualTo(None)
    assertThat(prefs.numberFormat.get()).isEqualTo(CommaDot)
    assertThat(prefs.hideFraction.get()).isEqualTo(false)
    assertThat(prefs.symbolPosition.get()).isEqualTo(BeforeAmount)
    assertThat(prefs.spaceBetweenAmountAndSymbol.get()).isEqualTo(false)
    assertThat(prefs.dateFormat.get()).isEqualTo(MmDdYyyy)
    assertThat(prefs.firstDayOfWeek.get()).isEqualTo(Sunday)
    assertThat(prefs.isPrivacyEnabled.get()).isEqualTo(false)
  }

  @Test
  fun `Reads upstream's stored values`() = runPrefsTest { prefs, dao, _ ->
    dao[SyncedPrefKey.Global.DefaultCurrencyCode] = "EUR"
    dao[SyncedPrefKey.Global.NumberFormat] = "dot-comma"
    dao[SyncedPrefKey.Global.HideFraction] = "true"
    dao[SyncedPrefKey.Global.CurrencySymbolPosition] = "after"
    dao[SyncedPrefKey.Global.CurrencySpaceBetweenAmountAndSymbol] = "true"
    dao[SyncedPrefKey.Global.DateFormat] = "dd.MM.yyyy"
    dao[SyncedPrefKey.Global.FirstDayOfWeekIdx] = "1"
    dao[SyncedPrefKey.Global.IsPrivacyEnabled] = "true"

    assertThat(prefs.currency.get()).isEqualTo(Euro)
    assertThat(prefs.numberFormat.get()).isEqualTo(DotComma)
    assertThat(prefs.hideFraction.get()).isEqualTo(true)
    assertThat(prefs.symbolPosition.get()).isEqualTo(AfterAmount)
    assertThat(prefs.spaceBetweenAmountAndSymbol.get()).isEqualTo(true)
    assertThat(prefs.dateFormat.get()).isEqualTo(DdMmYyyyDot)
    assertThat(prefs.firstDayOfWeek.get()).isEqualTo(Monday)
    assertThat(prefs.isPrivacyEnabled.get()).isEqualTo(true)
  }

  @Test
  fun `Unrecognised currency falls back to none`() = runPrefsTest { prefs, dao, _ ->
    dao[SyncedPrefKey.Global.DefaultCurrencyCode] = "XYZ"
    assertThat(prefs.currency.get()).isEqualTo(None)
  }

  @Test
  fun `Observes changes from sync`() = runPrefsTest { prefs, dao, _ ->
    prefs.currency.asFlow().test {
      assertThatNextEmissionIsEqualTo(None)
      dao[SyncedPrefKey.Global.DefaultCurrencyCode] = "GBP"
      assertThatNextEmissionIsEqualTo(PoundSterling)
    }
  }

  @Test
  fun `Setting a pref syncs it in upstream's format`() = runPrefsTest { prefs, _, sync ->
    prefs.symbolPosition.set(AfterAmount)
    prefs.firstDayOfWeek.set(Saturday)
    prefs.hideFraction.set(null)

    assertThat(sync.rows())
      .containsExactly(
        "currencySymbolPosition" to MessageValue.String("after"),
        "firstDayOfWeekIdx" to MessageValue.String("6"),
        "hideFraction" to MessageValue.Null,
      )
  }

  @Test
  fun `Changing currency also sets formats to suit it`() = runPrefsTest { prefs, _, sync ->
    prefs.changeCurrency(SwissFranc)

    assertThat(sync.rows())
      .containsExactly(
        "defaultCurrencyCode" to MessageValue.String("CHF"),
        "numberFormat" to MessageValue.String(Currency.SwissFranc.numberFormat.value),
        "hideFraction" to MessageValue.String("false"),
        "currencySpaceBetweenAmountAndSymbol" to MessageValue.String("true"),
        "currencySymbolPosition" to MessageValue.String("before"),
      )
  }

  @Test
  fun `Changing to no currency leaves other formats alone`() = runPrefsTest { prefs, _, sync ->
    prefs.changeCurrency(None)

    assertThat(sync.rows()).containsExactly("defaultCurrencyCode" to MessageValue.String(""))
  }

  private fun TestBudgetSync.rows() = changes.map { it.row to it.value }

  private fun runPrefsTest(
    action: suspend (BudgetFormatPreferencesImpl, PreferencesDao, TestBudgetSync) -> Unit,
  ) = runDatabaseTest { scope ->
    val dao =
      PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    val sync = TestBudgetSync()
    action(BudgetFormatPreferencesImpl(dao, sync), dao, sync)
  }
}
