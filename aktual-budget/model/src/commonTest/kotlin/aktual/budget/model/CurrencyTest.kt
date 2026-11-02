package aktual.budget.model

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

class CurrencyTest {
  @Test
  fun `Existing ordinals stay stable`() {
    // Currency is persisted by ordinal, so these must never move. Append new entries at the end.
    assertThat(Currency.entries.take(LEGACY.size).map { it.name })
      .containsExactly(*LEGACY.toTypedArray())
  }

  private companion object {
    val LEGACY =
      listOf(
        "None",
        "UaeDirham",
        "ArgentinianPeso",
        "AustralianDollar",
        "BrazilianReal",
        "BelarusianRuble",
        "CanadianDollar",
        "SwissFranc",
        "YuanRenminbi",
        "ColombianPeso",
        "CostaRicanColon",
        "CzechKoruna",
        "DanishKrone",
        "EgyptianPound",
        "Euro",
        "PoundSterling",
        "GuatemalanQuetzal",
        "HongKongDollar",
        "HungarianForint",
        "IndonesianRupiah",
        "IndianRupee",
        "JamaicanDollar",
        "JapaneseYen",
        "SouthKoreanWon",
        "SriLankanRupee",
        "MoldovanLeu",
        "MalaysianRinggit",
        "PhilippinePeso",
        "PolishZloty",
        "QatariRiyal",
        "RomanianLeu",
        "SerbianDinar",
        "RussianRuble",
        "SaudiRiyal",
        "SwedishKrona",
        "SingaporeDollar",
        "ThaiBaht",
        "TurkishLira",
        "UkrainianHryvnia",
        "UsDollar",
        "UzbekSoum",
        "VietnameseDong",
        "SouthAfricanRand",
        "BangladeshiTaka",
        "NorwegianKrone",
        "TanzanianShilling",
      )
  }
}
