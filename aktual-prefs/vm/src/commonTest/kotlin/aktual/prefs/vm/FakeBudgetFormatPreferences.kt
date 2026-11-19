package aktual.prefs.vm

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.prefs.BudgetFormatPreferences
import aktual.test.InMemoryPreference

internal class FakeBudgetFormatPreferences : BudgetFormatPreferences {
  override val numberFormat = InMemoryPreference(NumberFormat.Default)
  override val hideFraction = InMemoryPreference(false)
  override val currency = InMemoryPreference(Currency.None)
  override val symbolPosition = InMemoryPreference(CurrencySymbolPosition.Default)
  override val spaceBetweenAmountAndSymbol = InMemoryPreference(false)
  override val dateFormat = InMemoryPreference(DateFormat.Default)
  override val firstDayOfWeek = InMemoryPreference(FirstDayOfWeek.Default)
  override val isPrivacyEnabled = InMemoryPreference(false)

  override suspend fun changeCurrency(currency: Currency) = this.currency.set(currency)
}
