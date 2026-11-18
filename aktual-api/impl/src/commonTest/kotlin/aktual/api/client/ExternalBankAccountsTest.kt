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
    assertThat(externalBalance(27903.6 + 55807.2, inCents = false)).isEqualTo(Amount(8371080))
    assertThat(externalBalance(1234.5, inCents = false)).isEqualTo(Amount(123450))
    assertThat(externalBalance(-100.0, inCents = false)).isEqualTo(Amount(-10000))
    assertThat(externalBalance(0.0, inCents = false)).isEqualTo(Amount(0))
  }

  @Test
  fun `Keeps Enable Banking balances, which are already in cents`() {
    assertThat(externalBalance(123456.0, inCents = true)).isEqualTo(Amount(123456))
  }

  @Test
  fun `Returns unknown for a missing or invalid balance`() {
    assertThat(externalBalance(null, inCents = false)).isNull()
    assertThat(externalBalance(NaN, inCents = false)).isNull()
    assertThat(externalBalance(POSITIVE_INFINITY, inCents = false)).isNull()
    assertThat(externalBalance(1e14, inCents = false)).isNull()
    assertThat(externalBalance(1234.5, inCents = true)).isNull()
  }

  @Test
  fun `Parses string balances, treating blank or invalid ones as unknown`() {
    val accounts =
      listOf(
          JsonPrimitive("-100"),
          JsonPrimitive("   "),
          JsonPrimitive("abc"),
          JsonPrimitive("NaN"),
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

    assertThat(balances).containsExactly(Amount(-10000), null, null, null, null)
  }
}
