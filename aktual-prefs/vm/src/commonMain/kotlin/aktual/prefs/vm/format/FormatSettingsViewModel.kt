package aktual.prefs.vm.format

import aktual.budget.model.Currency
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.CurrencyPreferences
import aktual.prefs.FormatPreferences
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.collectAsState
import aktual.prefs.vm.launchAndSet
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class FormatSettingsViewModel
internal constructor(
  preferences: FormatPreferences,
  currencies: CurrencyPreferences,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val state: StateFlow<FormatSettingsState> =
    viewModelScope.launchMolecule(Immediate) {
      val numberFormat by collectAsState(preferences.numberFormat)
      val dateFormat by collectAsState(preferences.dateFormat)
      val firstDayOfWeek by collectAsState(preferences.firstDayOfWeek)
      val hideFraction by collectAsState(preferences.hideFraction)
      val distanceUnit by collectAsState(preferences.distanceUnit)
      val currency by collectAsState(currencies.currency)
      val symbolPosition by collectAsState(currencies.symbolPosition)
      val spaceBetweenAmountAndSymbol by collectAsState(currencies.spaceBetweenAmountAndSymbol)
      FormatSettingsState(
        numberFormat =
          ListPreference(
            value = numberFormat,
            onChange = { launchAndSet(preferences.numberFormat, it) },
          ),
        dateFormat =
          ListPreference(
            value = dateFormat,
            onChange = { launchAndSet(preferences.dateFormat, it) },
          ),
        firstDayOfWeek =
          ListPreference(
            value = firstDayOfWeek,
            onChange = { launchAndSet(preferences.firstDayOfWeek, it) },
          ),
        hideFraction =
          BooleanPreference(
            value = hideFraction,
            onChange = { launchAndSet(preferences.hideFraction, it) },
          ),
        currency =
          ListPreference(
            value = currency,
            options = CurrencyOptions,
            onChange = { launchAndSet(currencies.currency, it) },
          ),
        symbolPosition =
          ListPreference(
            value = symbolPosition,
            enabled = currency != None,
            onChange = { launchAndSet(currencies.symbolPosition, it) },
          ),
        spaceBetweenAmountAndSymbol =
          BooleanPreference(
            value = spaceBetweenAmountAndSymbol,
            enabled = currency != None,
            onChange = { launchAndSet(currencies.spaceBetweenAmountAndSymbol, it) },
          ),
        distanceUnit =
          ListPreference(
            value = distanceUnit,
            onChange = { launchAndSet(preferences.distanceUnit, it) },
          ),
      )
    }
}

private val CurrencyOptions = Currency.entries.filter { it != Unknown }.toImmutableList()
