package aktual.budget.transactions.vm

import aktual.budget.db.dao.TransactionRow
import aktual.budget.model.TransactionId
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class TransactionMappingTest {
  @Test
  fun `A recurring schedule's transaction is marked as recurring`() {
    assertThat(row(scheduleRecurring = true).toTransaction(balance = null).scheduleRecurring)
      .isEqualTo(true)
  }

  @Test
  fun `A one-off schedule's transaction isn't marked as recurring`() {
    assertThat(row(scheduleRecurring = false).toTransaction(balance = null).scheduleRecurring)
      .isEqualTo(false)
  }

  @Test
  fun `A transaction without a schedule has no recurrence`() {
    assertThat(row(scheduleRecurring = null).toTransaction(balance = null).scheduleRecurring)
      .isNull()
  }

  private fun row(scheduleRecurring: Boolean?) =
    TransactionRow(
      id = TransactionId("t1"),
      date = LocalDate(2026, 1, 1),
      accountName = null,
      payeeName = null,
      transferAccountName = null,
      notes = null,
      categoryName = null,
      amount = 0,
      isParent = false,
      isChild = false,
      needsCategory = false,
      offBudget = false,
      isTransfer = false,
      scheduleRecurring = scheduleRecurring,
    )
}
