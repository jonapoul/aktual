package aktual.budget.model

import androidx.compose.runtime.Immutable
import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Currency(
  val code: String,
  val symbol: String,
  val decimalPlaces: Int,
  val numberFormat: NumberFormat,
  val symbolPosition: CurrencySymbolPosition,
) {
  @SerialName("")
  None(
    code = "",
    symbol = "",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("AED")
  UaeDirham(
    code = "AED",
    symbol = "د.إ",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = AfterAmount,
  ),
  @SerialName("ARS")
  ArgentinianPeso(
    code = "ARS",
    symbol = "Arg$",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("AUD")
  AustralianDollar(
    code = "AUD",
    symbol = "A$",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("BDT")
  BangladeshiTaka(
    code = "BDT",
    symbol = "৳",
    decimalPlaces = 2,
    numberFormat = CommaDotIn,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("BRL")
  BrazilianReal(
    code = "BRL",
    symbol = "R$",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("BYN")
  BelarusianRuble(
    code = "BYN",
    symbol = "Br",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("CAD")
  CanadianDollar(
    code = "CAD",
    symbol = "CA$",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("CHF")
  SwissFranc(
    code = "CHF",
    symbol = "CHF",
    decimalPlaces = 2,
    numberFormat = ApostropheDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("CNY")
  YuanRenminbi(
    code = "CNY",
    symbol = "¥",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("COP")
  ColombianPeso(
    code = "COP",
    symbol = "Col$",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("CRC")
  CostaRicanColon(
    code = "CRC",
    symbol = "₡",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("CZK")
  CzechKoruna(
    code = "CZK",
    symbol = "Kč",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("DKK")
  DanishKrone(
    code = "DKK",
    symbol = "kr",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("EGP")
  EgyptianPound(
    code = "EGP",
    symbol = "ج.م",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = AfterAmount,
  ),
  @SerialName("EUR")
  Euro(
    code = "EUR",
    symbol = "€",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("GBP")
  PoundSterling(
    code = "GBP",
    symbol = "£",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("GTQ")
  GuatemalanQuetzal(
    code = "GTQ",
    symbol = "Q",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("HKD")
  HongKongDollar(
    code = "HKD",
    symbol = "HK$",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("HUF")
  HungarianForint(
    code = "HUF",
    symbol = "Ft",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("IDR")
  IndonesianRupiah(
    code = "IDR",
    symbol = "Rp",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("INR")
  IndianRupee(
    code = "INR",
    symbol = "₹",
    decimalPlaces = 2,
    numberFormat = CommaDotIn,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("JMD")
  JamaicanDollar(
    code = "JMD",
    symbol = "J$",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("JPY")
  JapaneseYen(
    code = "JPY",
    symbol = "¥",
    decimalPlaces = 0,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("KRW")
  SouthKoreanWon(
    code = "KRW",
    symbol = "₩",
    decimalPlaces = 0,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("LKR")
  SriLankanRupee(
    code = "LKR",
    symbol = "Rs.",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("MDL")
  MoldovanLeu(
    code = "MDL",
    symbol = "L",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("MYR")
  MalaysianRinggit(
    code = "MYR",
    symbol = "RM",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("NOK")
  NorwegianKrone(
    code = "NOK",
    symbol = "kr",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("PHP")
  PhilippinePeso(
    code = "PHP",
    symbol = "₱",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("PLN")
  PolishZloty(
    code = "PLN",
    symbol = "zł",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("QAR")
  QatariRiyal(
    code = "QAR",
    symbol = "ر.ق",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = AfterAmount,
  ),
  @SerialName("RON")
  RomanianLeu(
    code = "RON",
    symbol = "lei",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("RSD")
  SerbianDinar(
    code = "RSD",
    symbol = "дин",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("RUB")
  RussianRuble(
    code = "RUB",
    symbol = "₽",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("SAR")
  SaudiRiyal(
    code = "SAR",
    symbol = "ر.س",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = AfterAmount,
  ),
  @SerialName("SEK")
  SwedishKrona(
    code = "SEK",
    symbol = "kr",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("SGD")
  SingaporeDollar(
    code = "SGD",
    symbol = "S$",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("THB")
  ThaiBaht(
    code = "THB",
    symbol = "฿",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("TRY")
  TurkishLira(
    code = "TRY",
    symbol = "₺",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("TZS")
  TanzanianShilling(
    code = "TZS",
    symbol = "TSh",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("UAH")
  UkrainianHryvnia(
    code = "UAH",
    symbol = "₴",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("USD")
  UsDollar(
    code = "USD",
    symbol = "$",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  ),
  @SerialName("UZS")
  UzbekSoum(
    code = "UZS",
    symbol = "UZS",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("VND")
  VietnameseDong(
    code = "VND",
    symbol = "₫",
    decimalPlaces = 2,
    numberFormat = DotComma,
    symbolPosition = AfterAmount,
  ),
  @SerialName("ZAR")
  SouthAfricanRand(
    code = "ZAR",
    symbol = "R",
    decimalPlaces = 2,
    numberFormat = SpaceComma,
    symbolPosition = BeforeAmount,
  ),
  @Fallback
  Unknown(
    code = "?",
    symbol = "?",
    decimalPlaces = 2,
    numberFormat = CommaDot,
    symbolPosition = BeforeAmount,
  );

  companion object {
    val Default = None
  }
}

enum class CurrencySymbolPosition {
  BeforeAmount,
  AfterAmount;

  companion object {
    val Default = BeforeAmount
  }
}

@Immutable
data class CurrencyConfig(
  val currency: Currency,
  val position: CurrencySymbolPosition,
  val includeSpace: Boolean,
)
