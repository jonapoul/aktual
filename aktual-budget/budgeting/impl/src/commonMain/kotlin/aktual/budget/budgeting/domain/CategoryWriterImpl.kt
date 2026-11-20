package aktual.budget.budgeting.domain

import aktual.budget.budgeting.domain.NameResult.Blank
import aktual.budget.budgeting.domain.NameResult.Duplicate
import aktual.budget.budgeting.domain.NameResult.Saved
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.db.dao.DatabaseTables.CATEGORY_MAPPING
import aktual.budget.db.dao.DatabaseTables.REFLECT_BUDGETS
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGETS
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.budget.utils.BudgetSync
import aktual.core.utils.UuidGenerator
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(BudgetScope::class)
class CategoryWriterImpl
internal constructor(
  private val budgetDao: BudgetDao,
  private val preferencesDao: PreferencesDao,
  private val budgetSync: BudgetSync,
  private val uuidGenerator: UuidGenerator,
) : CategoryWriter {
  // insertCategory()
  override suspend fun createCategory(
    group: CategoryGroupId,
    name: String,
  ): NameResult<CategoryId> {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return Blank
    val groupRow = budgetDao.categoryGroups().first { it.id == group }
    val siblings = budgetDao.categoryOrder().filter { it.group_id == group }
    if (siblings.any { it.name.equals(trimmed, ignoreCase = true) }) return Duplicate

    val id = uuidGenerator(::CategoryId)
    val orders = sortOrders(listOf(id to null) + siblings.map { it.id to it.sort_order })
    val row = id.value
    val changes = buildList {
      for ((category, order) in orders) {
        if (category != id) add(localChange(CATEGORIES, category.value, "sort_order", order))
      }
      add(localChange(CATEGORIES, row, "name", trimmed))
      add(localChange(CATEGORIES, row, "cat_group", group.value))
      add(localChange(CATEGORIES, row, "is_income", groupRow.is_income == true))
      add(localChange(CATEGORIES, row, "hidden", false))
      add(localChange(CATEGORIES, row, "sort_order", orders.getValue(id)))
      add(localChange(CATEGORY_MAPPING, row, "transferId", row))
    }
    budgetSync(changes)
    return Saved(id)
  }

  // insertCategoryGroup()
  override suspend fun createGroup(name: String): NameResult<CategoryGroupId> {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return Blank
    val groups = budgetDao.categoryGroups()
    if (groups.any { it.name.equals(trimmed, ignoreCase = true) }) return Duplicate

    val id = uuidGenerator(::CategoryGroupId)
    val last = groups.maxOfOrNull { it.sort_order ?: 0.0 } ?: 0.0
    val row = id.value
    budgetSync(
      localChange(CATEGORY_GROUPS, row, "name", trimmed),
      localChange(CATEGORY_GROUPS, row, "is_income", false),
      localChange(CATEGORY_GROUPS, row, "hidden", false),
      localChange(CATEGORY_GROUPS, row, "sort_order", last + SORT_INCREMENT),
    )
    return Saved(id)
  }

  override suspend fun renameCategory(id: CategoryId, name: String): NameResult<Unit> {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return Blank
    val categories = budgetDao.categoryOrder()
    val category = categories.first { it.id == id }
    if (category.name == trimmed) return Saved(Unit)
    val isDuplicate = categories.any {
      it.group_id == category.group_id && it.id != id && it.name.equals(trimmed, ignoreCase = true)
    }
    if (isDuplicate) return Duplicate
    budgetSync(localChange(CATEGORIES, id.value, "name", trimmed))
    return Saved(Unit)
  }

  override suspend fun renameGroup(id: CategoryGroupId, name: String): NameResult<Unit> {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return Blank
    val groups = budgetDao.categoryGroups()
    if (groups.first { it.id == id }.name == trimmed) return Saved(Unit)
    if (groups.any { it.id != id && it.name.equals(trimmed, ignoreCase = true) }) return Duplicate
    budgetSync(localChange(CATEGORY_GROUPS, id.value, "name", trimmed))
    return Saved(Unit)
  }

  override suspend fun setGroupHidden(id: CategoryGroupId, hidden: Boolean): UndoToken {
    budgetSync(localChange(CATEGORY_GROUPS, id.value, "hidden", hidden))
    return UndoToken(listOf(localChange(CATEGORY_GROUPS, id.value, "hidden", !hidden)))
  }

  override suspend fun isTransferRequired(categories: Collection<CategoryId>): Boolean {
    if (categories.isEmpty()) return false
    if (budgetDao.transactionCount(categories) > 0) return true
    val type = type()
    return categories.any { budgetDao.budgetsOfCategory(type, it).isNotEmpty() }
  }

  override suspend fun deleteCategory(id: CategoryId, transferTo: CategoryId?): UndoToken =
    delete(listOf(id), transferTo, group = null)

  override suspend fun deleteGroup(id: CategoryGroupId, transferTo: CategoryId?): UndoToken {
    val categories = budgetDao.categoryOrder().filter { it.group_id == id }.map { it.id }
    return delete(categories, transferTo, group = id)
  }

  // deleteCategory() and deleteCategoryGroup()
  private suspend fun delete(
    ids: List<CategoryId>,
    transferTo: CategoryId?,
    group: CategoryGroupId?,
  ): UndoToken {
    val batch = Batch()
    if (transferTo != null) {
      val categories = budgetDao.categories().associateBy { it.id }
      val target = requireNotNull(categories[transferTo]) { "No category $transferTo" }
      require(transferTo !in ids) { "Can't transfer $transferTo to itself" }
      require(ids.all { categories[it]?.is_income == target.is_income }) {
        "Cannot transfer between income and expense categories"
      }
      // Upstream only moves the budgets of expense categories
      if (target.is_income != true) batch.transferBudgets(ids, transferTo)
      for (id in ids) {
        for (mapped in budgetDao.mappedTo(id)) {
          batch.change(
            localChange(CATEGORY_MAPPING, mapped.value, "transferId", transferTo.value),
            previous = localChange(CATEGORY_MAPPING, mapped.value, "transferId", id.value),
          )
        }
      }
    }
    for (id in ids) {
      batch.change(tombstone(CATEGORIES, id.value), previous = untombstone(CATEGORIES, id.value))
    }
    if (group != null) {
      batch.change(
        tombstone(CATEGORY_GROUPS, group.value),
        previous = untombstone(CATEGORY_GROUPS, group.value),
      )
    }
    budgetSync(batch.changes)
    return UndoToken(batch.undo)
  }

  // doTransfer(): each month's budgets of the deleted categories are added to [transferTo]'s
  private suspend fun Batch.transferBudgets(ids: List<CategoryId>, transferTo: CategoryId) {
    val type = type()
    val table =
      when (type) {
        Envelope -> ZERO_BUDGETS
        Tracking -> REFLECT_BUDGETS
      }
    val totals =
      ids
        .flatMap { budgetDao.budgetsOfCategory(type, it) }
        .groupBy { it.month }
        .mapValues { (_, rows) -> rows.sumOf { it.amount.toLong() } }
        .filterValues { it != 0L }

    for ((month, total) in totals) {
      val existing = budgetDao.budgetRow(type, month, transferTo)
      if (existing != null) {
        val amount = existing.amount + Amount(total)
        change(
          localChange(table, existing.id, "amount", amount.toLong()),
          previous = localChange(table, existing.id, "amount", existing.amount.toLong()),
        )
      } else {
        // A new row can't be deleted, so undoing resets it to the zero it reads as
        val id = "${month.dbMonth}-${transferTo.value}"
        changes += localChange(table, id, "month", month.dbMonth)
        changes += localChange(table, id, "category", transferTo.value)
        change(
          localChange(table, id, "amount", total),
          previous = localChange(table, id, "amount", 0L),
        )
      }
    }
  }

  // isTrackingBudget()
  private suspend fun type(): BudgetType =
    BudgetType.from(preferencesDao[SyncedPrefKey.Global.BudgetType]) ?: Envelope

  private class Batch {
    val changes = mutableListOf<LocalChange>()
    val undo = mutableListOf<LocalChange>()

    fun change(change: LocalChange, previous: LocalChange) {
      changes += change
      undo += previous
    }
  }
}
