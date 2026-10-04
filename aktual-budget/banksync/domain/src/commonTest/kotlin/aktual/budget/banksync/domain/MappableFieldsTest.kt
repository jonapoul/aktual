package aktual.budget.banksync.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal class MappableFieldsTest {
  @Test
  fun `Only fields the transaction has are offered`() {
    val transaction = buildJsonObject {
      put("date", "2026-09-30")
      put("valueDate", "2026-09-29")
      put("payeeName", "Tesco")
      put("remittanceInformationUnstructuredArrayString", "Card payment")
      put("unknownField", "ignored")
    }

    assertThat(mappableFields(transaction))
      .isEqualTo(
        mapOf(
          MappedField.Date to
            listOf(FieldExample("date", "2026-09-30"), FieldExample("valueDate", "2026-09-29")),
          MappedField.Payee to
            listOf(
              FieldExample("payeeName", "Tesco"),
              FieldExample("remittanceInformationUnstructuredArrayString", "Card payment"),
            ),
          MappedField.Notes to
            listOf(FieldExample("remittanceInformationUnstructuredArrayString", "Card payment")),
        )
      )
  }

  @Test
  fun `Dotted fields are read from nested objects`() {
    val transaction = buildJsonObject {
      putJsonObject("merchant") { put("name", "Tesco Extra") }
      put("category", 12)
      put("transaction_id", buildJsonArray { add("a") })
    }

    val fields = mappableFields(transaction)

    assertThat(fields[Payee]).isEqualTo(listOf(FieldExample("merchant.name", "Tesco Extra")))
    assertThat(fields[Notes])
      .isEqualTo(
        listOf(
          FieldExample("category", "12"),
          FieldExample("merchant.name", "Tesco Extra"),
          FieldExample("transaction_id", "a"),
        )
      )
  }

  @Test
  fun `The newest synced transaction in each direction is the example`() = runBankSyncTest {
    val loader = MappableFieldsLoader(dao)
    assertThat(loader.load(ACCOUNT, Payment)).isNull()

    import(
      bankTx(amount = "-1.00", date = "2026-09-28", payeeName = "Old", transactionId = "a"),
      bankTx(amount = "-2.00", date = "2026-09-30", payeeName = "New", transactionId = "b"),
      bankTx(amount = "3.00", date = "2026-09-29", payeeName = "Salary", transactionId = "c"),
    )

    assertThat(loader.load(ACCOUNT, Payment)?.get(Payee))
      .isEqualTo(listOf(FieldExample("payeeName", "New")))
    assertThat(loader.load(ACCOUNT, Deposit)?.get(Payee))
      .isEqualTo(listOf(FieldExample("payeeName", "Salary")))
  }
}
