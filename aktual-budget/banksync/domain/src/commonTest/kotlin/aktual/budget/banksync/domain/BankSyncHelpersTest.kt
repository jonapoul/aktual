package aktual.budget.banksync.domain

import aktual.api.model.banksync.BankSyncTransaction
import aktual.budget.db.dao.BankSyncCandidate
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate

internal class BankSyncHelpersTest {
  private val download =
    BankSyncDownload(
      transactions =
        listOf(
            bankTx("-12.34"),
            bankTx("0.1"),
            bankTx("-5.00", booked = false),
          )
          .map(::BankSyncTransaction),
      currentBalance = Amount(10_000),
    )

  @Test
  fun `Starting balance subtracts the downloaded transactions`() {
    // 10000 - (-1234 + 10 - 500)
    assertThat(startingBalance(AccountSyncSource.SimpleFin, download, importPending = true))
      .isEqualTo(Amount(11_724))
    assertThat(startingBalance(AccountSyncSource.Akahu, download, importPending = false))
      .isEqualTo(Amount(11_724))
    assertThat(startingBalance(AccountSyncSource.PluggyAi, download, importPending = false))
      .isEqualTo(Amount(11_724))
  }

  @Test
  fun `Enable Banking only subtracts pending transactions when they're imported`() {
    assertThat(startingBalance(AccountSyncSource.EnableBanking, download, importPending = true))
      .isEqualTo(Amount(11_724))
    assertThat(startingBalance(AccountSyncSource.EnableBanking, download, importPending = false))
      .isEqualTo(Amount(11_224))
  }

  @Test
  fun `Other sources take the current balance as it is`() {
    assertThat(startingBalance(AccountSyncSource.GoCardless, download, importPending = true))
      .isEqualTo(Amount(10_000))
    assertThat(startingBalance(null, download.copy(currentBalance = null), importPending = true))
      .isEqualTo(Amount(0))
  }

  @Test
  fun `Fuzzy candidates are ordered by distance, then without an imported_id first`() {
    val date = LocalDate(2026, 9, 30)
    val far = candidate("far", LocalDate(2026, 9, 25))
    val linked = candidate("linked", LocalDate(2026, 10, 1), importedId = "t1")
    val unlinked = candidate("unlinked", LocalDate(2026, 9, 29))
    val exact = candidate("exact", date, importedId = "t2")

    val sorted = listOf(far, linked, unlinked, exact).sortedWith(fuzzyMatchOrder(date))

    assertThat(sorted).containsExactly(exact, unlinked, linked, far)
  }

  private fun candidate(id: String, date: LocalDate, importedId: String? = null) =
    BankSyncCandidate(id = TransactionId(id), date = date, importedId = importedId)
}
