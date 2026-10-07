package aktual.budget.banksync.domain

import aktual.budget.banksync.domain.SyncMappings.Companion.parse
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

    assertThat(parse(json))
      .isEqualTo(
        SyncMappings(
          payment =
            FieldMapping(date = "bookingDate", payee = "creditorName", notes = "remittance"),
          deposit = FieldMapping(date = "valueDate", payee = "debtorName", notes = null),
        ),
      )
  }

  @Test
  fun `A missing direction falls back to the default`() {
    val mappings = parse("""{"payment":{"date":"a","payee":"b","notes":"c"}}""")

    assertThat(mappings.deposit).isEqualTo(SyncMappings.Default.deposit)
  }

  @Test
  fun `Anything but a JSON object fails`() {
    assertFailure { parse("[]") }
      .isInstanceOf<IllegalArgumentException>()
      .hasMessage("Failed to parse mapping: Invalid mapping format")
    assertFailure { parse("{") }.isInstanceOf<IllegalArgumentException>()
  }

  @Test
  fun `Encoded mappings parse back`() {
    val mappings =
      SyncMappings.Default.with(Payment, Payee, "creditorName").with(Deposit, Notes, "category")

    assertThat(mappings.encode())
      .isEqualTo(
        """
        {"payment":{"date":"date","payee":"creditorName","notes":"notes"},
        "deposit":{"date":"date","payee":"payeeName","notes":"category"}}
        """
          .trimIndent()
          .replace("\n", ""),
      )
    assertThat(parse(mappings.encode())).isEqualTo(mappings)
  }

  @Test
  fun `Unnamed fields are left out`() {
    val mappings =
      SyncMappings(
        payment = FieldMapping(date = "date", payee = null, notes = null),
        deposit = FieldMapping(date = null, payee = null, notes = null),
      )

    assertThat(mappings.encode()).isEqualTo("""{"payment":{"date":"date"},"deposit":{}}""")
  }
}
