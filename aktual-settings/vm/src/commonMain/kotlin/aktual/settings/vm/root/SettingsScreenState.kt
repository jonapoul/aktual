package aktual.settings.vm.root

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.settings.vm.BooleanPreference
import aktual.settings.vm.ListPreference
import aktual.settings.vm.SliderPreference
import androidx.compose.runtime.Immutable

@Immutable
data class SettingsScreenState(
  val systemUi: SystemUiConfigState,
  val format: FormatConfigState,
  val currency: CurrencyConfigState,
)

@Immutable
data class SystemUiConfigState(
  val showStatusBar: BooleanPreference,
  val blurTopBar: BooleanPreference,
  val blurStatusBar: BooleanPreference,
  val blurDialogs: BooleanPreference,
  val blurRadiusDp: SliderPreference,
  val blurAlpha: SliderPreference,
)

fun BlurRadiusPreference(
  value: Float,
  enabled: Boolean = true,
  onChange: (Float) -> Unit = {},
): SliderPreference =
  SliderPreference(value = value, range = 0f..20f, enabled = enabled, onChange = onChange)

fun BlurAlphaPreference(
  value: Float,
  enabled: Boolean = true,
  onChange: (Float) -> Unit = {},
): SliderPreference =
  SliderPreference(value = value, range = 0f..1f, enabled = enabled, onChange = onChange)

@Immutable
data class FormatConfigState(
  val numberFormat: ListPreference<NumberFormat>,
  val dateFormat: ListPreference<DateFormat>,
  val firstDayOfWeek: ListPreference<FirstDayOfWeek>,
  val hideFraction: BooleanPreference,
)

@Immutable
data class CurrencyConfigState(
  val currency: ListPreference<Currency>,
  val symbolPosition: ListPreference<CurrencySymbolPosition>,
  val spaceBetweenAmountAndSymbol: BooleanPreference,
)
