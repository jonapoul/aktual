package aktual.budget.banksync.domain

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

internal class SyncMappingsTest {
  @Test
  fun `Parse both directions`() {
    val json =
      """
      {
        "payment": { "date": "bookingDate", "payee": "creditorName", "notes": "remittance" },
        "deposit": { "date": "valueDate", "payee": "debtorName" }
      }
      """
        .trimIndent()

    assertThat(SyncMappings.parse(json))
      .isEqualTo(
        SyncMappings(
          payment =
            FieldMapping(date = "bookingDate", payee = "creditorName", notes = "remittance"),
          deposit = FieldMapping(date = "valueDate", payee = "debtorName", notes = null),
        )
      )
  }

  @Test
  fun `A missing direction falls back to the default`() {
    val mappings = SyncMappings.parse("""{"payment":{"date":"a","payee":"b","notes":"c"}}""")

    assertThat(mappings.deposit).isEqualTo(SyncMappings.Default.deposit)
  }

  @Test
  fun `Anything but a JSON object fails`() {
    assertFailure { SyncMappings.parse("[]") }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("Failed to parse mapping: Invalid mapping format")
    assertFailure { SyncMappings.parse("{") }.isInstanceOf<IllegalArgumentException>()
  }
}
