package aktual.api.client

import aktual.budget.model.Amount
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// Ported from the externalBalanceToInteger tests in upstream's
// packages/desktop-client/src/components/modals/SelectLinkedAccountsModal.test.ts
// (actualbudget/actual#9529)
class ExternalBankAccountsTest {
  @Test
  fun `Converts decimal balances to integer cents`() {
    assertThat(decimalBalance(27903.6 + 55807.2)).isEqualTo(Amount(8371080))
    assertThat(decimalBalance(1234.5)).isEqualTo(Amount(123450))
    assertThat(decimalBalance(-100.0)).isEqualTo(Amount(-10000))
    assertThat(decimalBalance(0.0)).isEqualTo(Amount(0))
  }

  @Test
  fun `Keeps Enable Banking balances, which are already in cents`() {
    assertThat(centsBalance(123456.0)).isEqualTo(Amount(123456))
  }

  @Test
  fun `Returns unknown for a missing or invalid balance`() {
    assertThat(decimalBalance(null)).isNull()
    assertThat(decimalBalance(NaN)).isNull()
    assertThat(decimalBalance(POSITIVE_INFINITY)).isNull()
    assertThat(decimalBalance(1e14)).isNull()
    assertThat(centsBalance(null)).isNull()
    assertThat(centsBalance(NaN)).isNull()
    assertThat(centsBalance(1234.5)).isNull()
  }

  @Test
  fun `Keeps balances up to the largest safe number`() {
    assertThat(decimalBalance(22517998136852.47)).isEqualTo(Amount(2251799813685247))
    assertThat(decimalBalance(-22517998136852.47)).isEqualTo(Amount(-2251799813685247))
    assertThat(decimalBalance(22517998136852.48)).isNull()
    assertThat(centsBalance(2251799813685247.0)).isEqualTo(Amount(2251799813685247))
    assertThat(centsBalance(2251799813685248.0)).isNull()
  }

  @Test
  fun `Returns unknown for balances that overflow a Long`() {
    assertThat(decimalBalance(1e300)).isNull()
    assertThat(decimalBalance(-1e300)).isNull()
    assertThat(centsBalance(1e300)).isNull()
    assertThat(centsBalance(-1e300)).isNull()
  }

  @Test
  fun `Parses string balances, treating blank or invalid ones as unknown`() {
    val accounts =
      listOf(
          JsonPrimitive("-100"),
          JsonPrimitive("   "),
          JsonPrimitive("abc"),
          JsonPrimitive("NaN"),
          JsonPrimitive("100d"),
          JsonPrimitive("0x1p4"),
          JsonPrimitive(" 12.5 "),
        )
        .plus(JsonNull)
        .mapIndexed { i, balance ->
          buildJsonObject {
            put("id", "ACT-$i")
            put("name", "Account $i")
            put("balance", balance)
          }
        }

    val balances = externalAccounts(SimpleFin, JsonArray(accounts)).map { it.balance }

    assertThat(balances)
      .containsExactly(Amount(-10000), null, null, null, null, null, Amount(1250), null)
  }

  @Test
  fun `Reads Pluggy ai balances, treating oversized ones as unknown`() {
    val accounts =
      JsonArray(
        listOf(
          buildJsonObject {
            put("id", "bank")
            put("type", "BANK")
            put(
              "bankData",
              buildJsonObject {
                put("closingBalance", 10.5)
                put("automaticallyInvestedBalance", 2.25)
              },
            )
          },
          buildJsonObject {
            put("id", "huge")
            put("type", "BANK")
            put("bankData", buildJsonObject { put("closingBalance", 1e300) })
          },
          buildJsonObject {
            put("id", "card")
            put("type", "CREDIT")
            put("balance", -12.34)
          },
        ),
      )

    val balances = externalAccounts(PluggyAi, accounts).map { it.balance }

    assertThat(balances).containsExactly(Amount(1275), null, Amount(-1234))
  }

  @Test
  fun `Reads Akahu balances, treating missing ones as unknown`() {
    val accounts =
      JsonArray(
        listOf(
          buildJsonObject {
            put("_id", "valid")
            put("balance", buildJsonObject { put("current", 50.5) })
          },
          buildJsonObject {
            put("_id", "missing")
            put("balance", buildJsonObject {})
          },
          buildJsonObject {
            put("_id", "invalid")
            put("balance", buildJsonObject { put("current", "abc") })
          },
        ),
      )

    val balances = externalAccounts(Akahu, accounts).map { it.balance }

    assertThat(balances).containsExactly(Amount(5050), null, null)
  }

  @Test
  fun `Reads Enable Banking balances, treating partial cents as unknown`() {
    val accounts =
      JsonArray(
        listOf(
          buildJsonObject {
            put("account_id", "whole")
            put("balance", 12345)
          },
          buildJsonObject {
            put("account_id", "partial")
            put("balance", 123.45)
          },
          buildJsonObject {
            put("account_id", "huge")
            put("balance", 1e300)
          },
        ),
      )

    val balances = enableBankingAccounts(accounts).map { it.balance }

    assertThat(balances).containsExactly(Amount(12345), null, null)
  }
}
