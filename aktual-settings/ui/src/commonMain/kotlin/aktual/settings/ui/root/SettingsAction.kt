package aktual.settings.ui.root

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface SettingsAction {
  data object NavBack : SettingsAction

  data object NavToThemeSettings : SettingsAction

  @JvmInline value class SetShowBottomBar(val value: Boolean) : SettingsAction

  @JvmInline value class SetNumberFormat(val value: NumberFormat) : SettingsAction

  @JvmInline value class SetHideFraction(val value: Boolean) : SettingsAction

  @JvmInline value class SetFirstDayOfWeek(val value: FirstDayOfWeek) : SettingsAction

  @JvmInline value class SetCurrency(val value: Currency) : SettingsAction

  @JvmInline
  value class SetCurrencySymbolPosition(val value: CurrencySymbolPosition) : SettingsAction

  @JvmInline value class SetCurrencySpace(val value: Boolean) : SettingsAction
}
