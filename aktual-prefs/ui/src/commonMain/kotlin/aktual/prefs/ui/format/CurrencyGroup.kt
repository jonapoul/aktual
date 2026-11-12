package aktual.prefs.ui.format

import aktual.budget.model.Amount
import aktual.budget.model.Currency
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.NumberFormatConfig
import aktual.core.icons.flags.Flag
import aktual.core.icons.material.CurrencyPound
import aktual.core.icons.material.LineEndArrowNotch
import aktual.core.icons.material.LineStartArrowNotch
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SpaceBar
import aktual.core.l10n.Strings
import aktual.core.ui.LocalCurrencyConfig
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.vm.format.FormatSettingsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun CurrencyGroup(state: FormatSettingsState) =
  PreferenceGroup(title = Strings.settingsCurrency, subtitle = Strings.settingsCurrencyDesc) {
    ListPreferenceItem(
      preference = state.currency,
      optionString = { c -> c.string() },
      optionSuffix = { c -> Text(c.symbol, textAlign = Center) },
      icon = MaterialIcons.CurrencyPound,
      title = Strings.settingsCurrencyDefault,
      subtitle = null,
      searchPlaceholder = Strings.settingsCurrencySearch,
      searchTerms = { c -> listOf(c.code, c.symbol) },
      optionPrefix = { c ->
        // ISO 4217 codes start with the issuing country's ISO 3166 code, or EU for the euro
        Flag(if (c == None || c == Unknown) null else c.code.take(2))
      },
    )
    ListPreferenceItem(
      preference = state.symbolPosition,
      optionString = { c -> c.string() },
      optionSuffix = null,
      icon =
        when (state.symbolPosition.value) {
          BeforeAmount -> MaterialIcons.LineStartArrowNotch
          AfterAmount -> MaterialIcons.LineEndArrowNotch
        },
      title = Strings.settingsCurrencySymbolPosition,
      subtitle = null,
    )
    BooleanPreferenceItem(
      preference = state.spaceBetweenAmountAndSymbol,
      icon = MaterialIcons.SpaceBar,
      title = Strings.settingsCurrencyAddSpace,
      subtitle = null,
    )
  }

@Composable
private fun Currency.string(): String =
  when (this) {
    None -> Strings.currencyNone
    UaeDirham -> Strings.currencyAed
    ArgentinianPeso -> Strings.currencyArs
    AustralianDollar -> Strings.currencyAud
    BrazilianReal -> Strings.currencyBrl
    BelarusianRuble -> Strings.currencyByn
    CanadianDollar -> Strings.currencyCad
    SwissFranc -> Strings.currencyChf
    YuanRenminbi -> Strings.currencyCny
    ColombianPeso -> Strings.currencyCop
    CostaRicanColon -> Strings.currencyCrc
    CzechKoruna -> Strings.currencyCzk
    DanishKrone -> Strings.currencyDkk
    EgyptianPound -> Strings.currencyEgp
    Euro -> Strings.currencyEur
    PoundSterling -> Strings.currencyGbp
    GuatemalanQuetzal -> Strings.currencyGtq
    HongKongDollar -> Strings.currencyHkd
    HungarianForint -> Strings.currencyHuf
    IndonesianRupiah -> Strings.currencyIdr
    IndianRupee -> Strings.currencyInr
    JamaicanDollar -> Strings.currencyJmd
    JapaneseYen -> Strings.currencyJpy
    SouthKoreanWon -> Strings.currencyKrw
    SriLankanRupee -> Strings.currencyLkr
    MoldovanLeu -> Strings.currencyMdl
    MalaysianRinggit -> Strings.currencyMyr
    PhilippinePeso -> Strings.currencyPhp
    PolishZloty -> Strings.currencyPln
    QatariRiyal -> Strings.currencyQar
    RomanianLeu -> Strings.currencyRon
    SerbianDinar -> Strings.currencyRsd
    RussianRuble -> Strings.currencyRub
    SaudiRiyal -> Strings.currencySar
    SwedishKrona -> Strings.currencySek
    SingaporeDollar -> Strings.currencySgd
    ThaiBaht -> Strings.currencyThb
    TurkishLira -> Strings.currencyTry
    UkrainianHryvnia -> Strings.currencyUah
    UsDollar -> Strings.currencyUsd
    UzbekSoum -> Strings.currencyUzs
    VietnameseDong -> Strings.currencyVnd
    SouthAfricanRand -> Strings.currencyZar
    BangladeshiTaka -> Strings.currencyBdt
    NorwegianKrone -> Strings.currencyNok
    TanzanianShilling -> Strings.currencyTzs
    Unknown -> Strings.currencyUnknown
  }

@Composable
private fun CurrencySymbolPosition.string(
  config: CurrencyConfig = LocalCurrencyConfig.current,
): String =
  when (this) {
    BeforeAmount ->
      Strings.settingsCurrencySymbolPositionBefore(symbolPositionString(config, position = this))

    AfterAmount ->
      Strings.settingsCurrencySymbolPositionAfter(symbolPositionString(config, position = this))
  }

private val EXAMPLE_AMOUNT = Amount(12300)
private val EXAMPLE_NUMBER_CONFIG = NumberFormatConfig(CommaDot, hideFraction = true)

private fun symbolPositionString(config: CurrencyConfig, position: CurrencySymbolPosition): String =
  EXAMPLE_AMOUNT.toString(
    numberFormatConfig = EXAMPLE_NUMBER_CONFIG,
    currencyConfig = config.copy(position = position),
    includeSign = false,
    isPrivacyEnabled = false,
  )
