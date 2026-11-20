package aktual.budget.budgeting.domain

import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import kotlin.jvm.JvmInline

/**
 * Creates, renames, hides and deletes categories and groups, as the category-* and category-group-*
 * handlers of packages/loot-core/src/server/budget/app.ts. Names are trimmed, and must be unique
 * within the group for categories and across all groups for groups, ignoring case.
 *
 * Deletes return an [UndoToken] for [BudgetWriter.undo].
 */
interface CategoryWriter {
  // Added at the top of the group, as upstream
  suspend fun createCategory(group: CategoryGroupId, name: String): NameResult<CategoryId>

  // Added after the last group
  suspend fun createGroup(name: String): NameResult<CategoryGroupId>

  suspend fun renameCategory(id: CategoryId, name: String): NameResult<Unit>

  suspend fun renameGroup(id: CategoryGroupId, name: String): NameResult<Unit>

  suspend fun setGroupHidden(id: CategoryGroupId, hidden: Boolean): UndoToken

  /**
   * Whether deleting these categories needs another category to take over their transactions and
   * budgeted amounts, as isCategoryTransferRequired()
   */
  suspend fun isTransferRequired(categories: Collection<CategoryId>): Boolean

  // [transferTo] takes over its transactions and, for expense categories, its budgeted amounts
  suspend fun deleteCategory(id: CategoryId, transferTo: CategoryId?): UndoToken

  // The same for each of its categories
  suspend fun deleteGroup(id: CategoryGroupId, transferTo: CategoryId?): UndoToken
}

sealed interface NameResult<out T> {
  @JvmInline value class Saved<T>(val value: T) : NameResult<T>

  data object Blank : NameResult<Nothing>

  data object Duplicate : NameResult<Nothing>
}
