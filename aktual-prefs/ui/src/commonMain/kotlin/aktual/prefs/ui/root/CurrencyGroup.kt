package aktual.prefs.ui.root

import aktual.budget.model.Amount
import aktual.budget.model.Currency
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.NumberFormat
import aktual.budget.model.NumberFormatConfig
import aktual.core.icons.material.CurrencyPound
import aktual.core.icons.material.LineEndArrowNotch
import aktual.core.icons.material.LineStartArrowNotch
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SpaceBar
import aktual.core.l10n.Strings
import aktual.core.ui.LocalCurrencyConfig
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.PreferenceGroup
import aktual.prefs.vm.root.CurrencyConfigState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
internal fun CurrencyGroup(state: CurrencyConfigState, modifier: Modifier = Modifier) {
  PreferenceGroup(
    title = Strings.settingsCurrency,
    subtitle = Strings.settingsCurrencyDesc,
    modifier = modifier,
  ) {
    ListPreferenceItem(
      preference = state.currency,
      optionString = { c -> c.string() },
      optionSuffix = { c -> Text(c.symbol, textAlign = TextAlign.Center) },
      icon = MaterialIcons.CurrencyPound,
      title = Strings.settingsCurrencyDefault,
      subtitle = null,
      includeBackground = false,
    )

    ListPreferenceItem(
      preference = state.symbolPosition,
      optionString = { c -> c.string() },
      optionSuffix = null,
      icon =
        when (state.symbolPosition.value) {
          CurrencySymbolPosition.BeforeAmount -> MaterialIcons.LineStartArrowNotch
          CurrencySymbolPosition.AfterAmount -> MaterialIcons.LineEndArrowNotch
        },
      title = Strings.settingsCurrencySymbolPosition,
      subtitle = null,
      includeBackground = false,
    )

    BooleanPreferenceItem(
      preference = state.spaceBetweenAmountAndSymbol,
      icon = MaterialIcons.SpaceBar,
      title = Strings.settingsCurrencyAddSpace,
      subtitle = null,
      includeBackground = false,
    )
  }
}

@Composable
private fun Currency.string(): String =
  when (this) {
    Currency.None -> Strings.currencyNone
    Currency.UaeDirham -> Strings.currencyAed
    Currency.ArgentinianPeso -> Strings.currencyArs
    Currency.AustralianDollar -> Strings.currencyAud
    Currency.BrazilianReal -> Strings.currencyBrl
    Currency.BelarusianRuble -> Strings.currencyByn
    Currency.CanadianDollar -> Strings.currencyCad
    Currency.SwissFranc -> Strings.currencyChf
    Currency.YuanRenminbi -> Strings.currencyCny
    Currency.ColombianPeso -> Strings.currencyCop
    Currency.CostaRicanColon -> Strings.currencyCrc
    Currency.CzechKoruna -> Strings.currencyCzk
    Currency.DanishKrone -> Strings.currencyDkk
    Currency.EgyptianPound -> Strings.currencyEgp
    Currency.Euro -> Strings.currencyEur
    Currency.PoundSterling -> Strings.currencyGbp
    Currency.GuatemalanQuetzal -> Strings.currencyGtq
    Currency.HongKongDollar -> Strings.currencyHkd
    Currency.HungarianForint -> Strings.currencyHuf
    Currency.IndonesianRupiah -> Strings.currencyIdr
    Currency.IndianRupee -> Strings.currencyInr
    Currency.JamaicanDollar -> Strings.currencyJmd
    Currency.JapaneseYen -> Strings.currencyJpy
    Currency.SouthKoreanWon -> Strings.currencyKrw
    Currency.SriLankanRupee -> Strings.currencyLkr
    Currency.MoldovanLeu -> Strings.currencyMdl
    Currency.MalaysianRinggit -> Strings.currencyMyr
    Currency.PhilippinePeso -> Strings.currencyPhp
    Currency.PolishZloty -> Strings.currencyPln
    Currency.QatariRiyal -> Strings.currencyQar
    Currency.RomanianLeu -> Strings.currencyRon
    Currency.SerbianDinar -> Strings.currencyRsd
    Currency.RussianRuble -> Strings.currencyRub
    Currency.SaudiRiyal -> Strings.currencySar
    Currency.SwedishKrona -> Strings.currencySek
    Currency.SingaporeDollar -> Strings.currencySgd
    Currency.ThaiBaht -> Strings.currencyThb
    Currency.TurkishLira -> Strings.currencyTry
    Currency.UkrainianHryvnia -> Strings.currencyUah
    Currency.UsDollar -> Strings.currencyUsd
    Currency.UzbekSoum -> Strings.currencyUzs
    Currency.VietnameseDong -> Strings.currencyVnd
  }

@Composable
private fun CurrencySymbolPosition.string(
  config: CurrencyConfig = LocalCurrencyConfig.current
): String =
  when (this) {
    CurrencySymbolPosition.BeforeAmount ->
      Strings.settingsCurrencySymbolPositionBefore(symbolPositionString(config, position = this))

    CurrencySymbolPosition.AfterAmount ->
      Strings.settingsCurrencySymbolPositionAfter(symbolPositionString(config, position = this))
  }

private val EXAMPLE_AMOUNT = Amount(12300)
private val EXAMPLE_NUMBER_CONFIG = NumberFormatConfig(NumberFormat.CommaDot, hideFraction = true)

private fun symbolPositionString(config: CurrencyConfig, position: CurrencySymbolPosition): String =
  EXAMPLE_AMOUNT.toString(
    numberFormatConfig = EXAMPLE_NUMBER_CONFIG,
    currencyConfig = config.copy(position = position),
    includeSign = false,
    isPrivacyEnabled = false,
  )
