package aktual.budget.budgeting.domain

import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import kotlinx.datetime.YearMonth

/**
 * The per-category and month-wide actions of packages/loot-core/src/server/budget/actions.ts. Each
 * call sends its changes in one batch, and the UI picks them up through [BudgetMonthCalculator].
 *
 * Each returns an [UndoToken] for [undo], as upstream wraps them in undoable(), or null if it
 * changed nothing.
 */
@Suppress("ComplexInterface")
interface BudgetWriter {
  suspend fun setBudget(month: YearMonth, category: CategoryId, amount: Amount): UndoToken?

  // This month and every later month in the budget's bounds
  suspend fun setCarryover(from: YearMonth, category: CategoryId, enabled: Boolean): UndoToken?

  suspend fun copySinglePreviousMonth(month: YearMonth, category: CategoryId): UndoToken?

  suspend fun setSingleAverage(month: YearMonth, category: CategoryId, months: Int): UndoToken?

  // Upstream's UI only offers this for tracking budgets
  suspend fun copyUntilYearEnd(month: YearMonth, category: CategoryId): UndoToken?

  suspend fun copyPreviousMonth(month: YearMonth): UndoToken?

  suspend fun setZero(month: YearMonth): UndoToken?

  suspend fun setAverage(month: YearMonth, months: Int): UndoToken?

  // Envelope budgets only from here on. A null category is To Budget

  suspend fun transferCategory(
    month: YearMonth,
    amount: Amount,
    from: CategoryId,
    to: CategoryId?,
  ): UndoToken?

  suspend fun transferAvailable(month: YearMonth, amount: Amount, category: CategoryId): UndoToken?

  // A null amount covers all of the overspending
  suspend fun coverOverspending(
    month: YearMonth,
    to: CategoryId,
    from: CategoryId?,
    amount: Amount? = null,
  ): UndoToken?

  // A null amount covers all of the overbudgeting
  suspend fun coverOverbudgeted(
    month: YearMonth,
    category: CategoryId,
    amount: Amount? = null,
  ): UndoToken?

  // Null if there was nothing to hold
  suspend fun holdForNextMonth(month: YearMonth, amount: Amount): UndoToken?

  suspend fun resetHold(month: YearMonth): UndoToken?

  suspend fun setCategoryHidden(category: CategoryId, hidden: Boolean): UndoToken

  // Writes back the values the token's action replaced, in one batch
  suspend fun undo(token: UndoToken)
}

/**
 * The cells an action changed, holding the values they had before it. A row the action created is
 * reset to the defaults it reads as, since sync can't delete it.
 */
data class UndoToken(val changes: List<LocalChange>)
