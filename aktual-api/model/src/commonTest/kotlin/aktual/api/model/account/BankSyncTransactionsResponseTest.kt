package aktual.api.model.account

import aktual.api.model.banksync.BankSyncAmount
import aktual.api.model.banksync.BankSyncBalance
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.core.model.AktualJson
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlin.test.Test

class BankSyncTransactionsResponseTest {
  @Test
  fun `Decode numeric amounts`() {
    // Pluggy.ai sends amounts as numbers rather than strings
    val json =
      """
      {
        "balances": [
          {
            "balanceAmount": { "amount": 123456, "currency": "BRL" },
            "balanceType": "expected",
            "referenceDate": "2026-09-30"
          }
        ],
        "startingBalance": 123456,
        "transactions": {
          "all": [
            {
              "booked": true,
              "date": "2026-09-28",
              "payeeName": "Mercado",
              "transactionAmount": { "amount": -12.5, "currency": "BRL" },
              "transactionId": "abc"
            }
          ]
        }
      }
      """
        .trimIndent()

    val response = AktualJson.decodeFromString<BankSyncTransactionsResponse>(json)

    assertThat(response).isInstanceOf<BankSyncTransactionsResponse.Success>().all {
      prop(BankSyncTransactionsResponse.Success::balances)
        .isEqualTo(
          [
            BankSyncBalance(
              balanceAmount = BankSyncAmount(amount = "123456", currency = "BRL"),
              balanceType = "expected",
              referenceDate = "2026-09-30",
            )
          ]
        )
      transform { it.transactions.all.single().amount }.isEqualTo("-12.5")
      transform { it.transactions.booked }.isEqualTo([])
    }
  }
}
