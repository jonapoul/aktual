package aktual.budget.budgeting.vm

import aktual.budget.budgeting.domain.BudgetWriter
import aktual.budget.budgeting.domain.UndoToken
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import kotlinx.datetime.YearMonth

// Records each call as the method name and its arguments
internal class RecordingBudgetWriter : BudgetWriter {
  val calls = mutableListOf<List<Any?>>()

  override suspend fun setBudget(month: YearMonth, category: CategoryId, amount: Amount) =
    record("setBudget", month, category, amount)

  override suspend fun setCarryover(from: YearMonth, category: CategoryId, enabled: Boolean) =
    record("setCarryover", from, category, enabled)

  override suspend fun copySinglePreviousMonth(month: YearMonth, category: CategoryId) =
    record("copySinglePreviousMonth", month, category)

  override suspend fun setSingleAverage(month: YearMonth, category: CategoryId, months: Int) =
    record("setSingleAverage", month, category, months)

  override suspend fun copyUntilYearEnd(month: YearMonth, category: CategoryId) =
    record("copyUntilYearEnd", month, category)

  override suspend fun copyPreviousMonth(month: YearMonth) = record("copyPreviousMonth", month)

  override suspend fun setZero(month: YearMonth) = record("setZero", month)

  override suspend fun setAverage(month: YearMonth, months: Int) =
    record("setAverage", month, months)

  override suspend fun transferCategory(
    month: YearMonth,
    amount: Amount,
    from: CategoryId,
    to: CategoryId?,
  ) = record("transferCategory", month, amount, from, to)

  override suspend fun transferAvailable(month: YearMonth, amount: Amount, category: CategoryId) =
    record("transferAvailable", month, amount, category)

  override suspend fun coverOverspending(
    month: YearMonth,
    to: CategoryId,
    from: CategoryId?,
    amount: Amount?,
  ) = record("coverOverspending", month, to, from, amount)

  override suspend fun coverOverbudgeted(month: YearMonth, category: CategoryId, amount: Amount?) =
    record("coverOverbudgeted", month, category, amount)

  override suspend fun holdForNextMonth(month: YearMonth, amount: Amount) =
    record("holdForNextMonth", month, amount)

  override suspend fun resetHold(month: YearMonth) = record("resetHold", month)

  override suspend fun undo(token: UndoToken) {
    calls += listOf("undo", token)
  }

  // A new token per call, so tests can tell them apart
  private fun record(vararg call: Any?): UndoToken {
    calls += call.toList()
    return UndoToken(emptyList())
  }
}
