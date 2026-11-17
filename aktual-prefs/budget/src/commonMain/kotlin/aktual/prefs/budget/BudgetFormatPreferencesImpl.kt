package aktual.prefs.budget

import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.LocalChange
import aktual.budget.model.NumberFormat
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.localChange
import aktual.budget.utils.BudgetSync
import aktual.di.BudgetScope
import aktual.prefs.BudgetFormatPreferences
import aktual.prefs.Preference
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(BudgetScope::class)
class BudgetFormatPreferencesImpl
internal constructor(
  private val dao: PreferencesDao,
  private val budgetSync: BudgetSync,
) : BudgetFormatPreferences {
  override val numberFormat: Preference<NumberFormat> =
    pref(
      key = SyncedPrefKey.Global.NumberFormat,
      default = Default,
      decode = { v -> NumberFormat.entries.firstOrNull { it.value == v } },
      encode = NumberFormat::value,
    )

  override val hideFraction: Preference<Boolean> =
    booleanPref(SyncedPrefKey.Global.HideFraction, default = false)

  override val currency: Preference<Currency> =
    pref(
      key = SyncedPrefKey.Global.DefaultCurrencyCode,
      default = None,
      decode = { code -> Currency.entries.firstOrNull { it != Unknown && it.code == code } },
      encode = Currency::code,
    )

  override val symbolPosition: Preference<CurrencySymbolPosition> =
    pref(
      key = SyncedPrefKey.Global.CurrencySymbolPosition,
      default = Default,
      decode = { v -> CurrencySymbolPosition.entries.firstOrNull { it.encoded() == v } },
      encode = { it.encoded() },
    )

  override val spaceBetweenAmountAndSymbol: Preference<Boolean> =
    booleanPref(SyncedPrefKey.Global.CurrencySpaceBetweenAmountAndSymbol, default = false)

  override val dateFormat: Preference<DateFormat> =
    pref(
      key = SyncedPrefKey.Global.DateFormat,
      default = Default,
      decode = { v -> DateFormat.entries.firstOrNull { it.value == v } },
      encode = DateFormat::value,
    )

  override val firstDayOfWeek: Preference<FirstDayOfWeek> =
    pref(
      key = SyncedPrefKey.Global.FirstDayOfWeekIdx,
      default = Default,
      decode = FirstDayOfWeek::from,
      encode = { it.value.toString() },
    )

  override suspend fun changeCurrency(currency: Currency) {
    val changes = buildList {
      add(change(SyncedPrefKey.Global.DefaultCurrencyCode, currency.code))
      if (currency != None) {
        // A currency code used as the symbol (e.g. "CHF 100") needs a space
        val includeSpace =
          currency.symbolPosition == AfterAmount || currency.symbol == currency.code
        add(change(SyncedPrefKey.Global.NumberFormat, currency.numberFormat.value))
        add(change(SyncedPrefKey.Global.HideFraction, (currency.decimalPlaces == 0).toString()))
        add(
          change(SyncedPrefKey.Global.CurrencySpaceBetweenAmountAndSymbol, includeSpace.toString()),
        )
        add(change(SyncedPrefKey.Global.CurrencySymbolPosition, currency.symbolPosition.encoded()))
      }
    }
    budgetSync(changes)
  }

  private fun booleanPref(key: SyncedPrefKey, default: Boolean): Preference<Boolean> =
    pref(key, default, decode = String::toBooleanStrictOrNull, encode = Boolean::toString)

  private fun <T : Any> pref(
    key: SyncedPrefKey,
    default: T,
    decode: (String) -> T?,
    encode: (T) -> String,
  ): Preference<T> = SyncedPreference(dao, budgetSync, key, default, decode, encode)

  private fun change(key: SyncedPrefKey, value: String?): LocalChange =
    localChange(PREFERENCES, key.key, column = "value", value)

  // Upstream stores these as "before" / "after"
  private fun CurrencySymbolPosition.encoded(): String =
    when (this) {
      BeforeAmount -> "before"
      AfterAmount -> "after"
    }

  private class SyncedPreference<T : Any>(
    private val dao: PreferencesDao,
    private val budgetSync: BudgetSync,
    private val key: SyncedPrefKey,
    override val default: T,
    private val decode: (String) -> T?,
    private val encode: (T) -> String,
  ) : Preference<T> {
    override suspend fun get(): T = parse(dao[key])

    override suspend fun set(value: T?) {
      budgetSync(localChange(PREFERENCES, key.key, column = "value", value?.let(encode)))
    }

    override fun asFlow(): Flow<T> = dao.observe(key).map(::parse).distinctUntilChanged()

    private fun parse(value: String?): T = value?.let(decode) ?: default
  }
}
