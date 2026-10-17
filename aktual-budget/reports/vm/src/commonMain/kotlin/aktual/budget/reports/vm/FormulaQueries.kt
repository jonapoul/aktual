package aktual.budget.reports.vm

import aktual.budget.budgeting.domain.BudgetMonthCalculator
import aktual.budget.db.FormulaAccounts
import aktual.budget.db.FormulaCategories
import aktual.budget.db.FormulaTransactions
import aktual.budget.db.dao.FormulaDao
import aktual.budget.formula.CategoryBudget
import aktual.budget.formula.FormulaDataSource
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import aktual.budget.rules.domain.FilterRow
import aktual.budget.rules.domain.TransactionFilter
import aktual.core.Calendar
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.yearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

// packages/desktop-client/src/hooks/useFormulaExecution.ts: the data behind a formula card's query
// functions, read from the budget. Reads are cached, so make one per evaluation
internal class FormulaQueries(
  private val queries: Map<String, FormulaQuery>,
  private val dao: FormulaDao,
  private val budgets: BudgetMonthCalculator,
  private val calendar: Calendar,
) : FormulaDataSource {
  private val transactions = hashMapOf<String, List<FormulaTransactions>>()
  private val budgetMonths = hashMapOf<YearMonth, Map<String, CategoryBudget>>()
  private var accounts: List<FormulaAccounts>? = null
  private var categories: List<FormulaCategories>? = null

  override suspend fun querySum(name: String): Long? = transactions(name)?.sumOf { it.amount }

  override suspend fun queryCount(name: String): Long? = transactions(name)?.size?.toLong()

  override suspend fun queryCategories(name: String): List<String>? {
    val query = queries[name] ?: return null
    val conditions =
      query.conditions.filter { it.customName == null && it.field in CATEGORY_FIELDS }
    return categoryIds(categories(), conditions, query.conditionsOp)
  }

  override suspend fun queryMonths(name: String): YearMonthRange? {
    val query = queries[name] ?: return null
    return query.months() ?: calendar.today().yearMonth.let { it..it }
  }

  override suspend fun accountBalance(account: String): Long? {
    val accounts = accounts()
    val match =
      accounts.firstOrNull { it.id.value == account }
        ?: accounts.firstOrNull { it.name == account }
        ?: return null
    return dao.accountBalance(match.id)
  }

  override suspend fun budgetMonth(month: YearMonth): Map<String, CategoryBudget> =
    budgetMonths.getOrPut(month) {
      val goals =
        dao.goals(month).mapNotNull { row -> row.category?.let { it.value to row.goal } }.toMap()
      budgets.observe(month).first().categories.associate { category ->
        category.id.value to
          CategoryBudget(
            budgeted = category.budgeted.toLong(),
            spent = category.spent.toLong(),
            goal = goals[category.id.value] ?: 0L,
            balance = category.balance.toLong(),
            carryover = category.carryover,
          )
      }
    }

  private suspend fun accounts(): List<FormulaAccounts> =
    accounts ?: dao.accounts().also { accounts = it }

  private suspend fun categories(): List<FormulaCategories> =
    categories ?: dao.categories().also { categories = it }

  private suspend fun transactions(name: String): List<FormulaTransactions>? {
    val query = queries[name] ?: return null
    transactions[name]?.let {
      return it
    }
    val months = query.months()
    val rows =
      if (months == null) {
        dao.transactions(LocalDate(MIN_YEAR, 1, 1), LocalDate(MAX_YEAR, 12, 31))
      } else {
        dao.transactions(months.start.firstDay, months.endInclusive.lastDay)
      }
    val filter = TransactionFilter.compile(query.conditions, query.conditionsOp ?: And)
    return rows.filter { filter.matches(it.toFilterRow()) }.also { transactions[name] = it }
  }

  private fun FormulaQuery.months(): YearMonthRange? = timeFrame?.let {
    resolveTimeRange(it, null, calendar.today(), latestTransaction = null)
  }

  // getCategoriesFromConditions(): what each condition matches, then the union of those for "or"
  // and the intersection for "and"
  private fun categoryIds(
    categories: List<FormulaCategories>,
    conditions: List<Condition>,
    op: ConditionOp?,
  ): List<String> {
    if (conditions.isEmpty()) {
      return categories.filter { it.is_income != true && !it.hidden }.map { it.id.value }
    }
    val matches = conditions.map { condition ->
      categories.filter { condition.matches(it) }.map { it.id.value }
    }
    return if (op == Or) {
      matches.flatten().distinct()
    } else {
      matches.reduce { acc, ids -> acc.filter { it in ids } }
    }
  }

  private fun Condition.matches(category: FormulaCategories): Boolean {
    val isGroup = field == CategoryGroup
    val key = if (isGroup) category.category_group.value else category.id.value
    val text = if (isGroup) category.category_group_name ?: key else category.name.orEmpty()
    val string = value.string()
    return when (operator) {
      Is -> string == key
      IsNot -> string != key
      OneOf -> value.contains(key)
      NotOneOf -> !value.contains(key)
      Contains -> string != null && text.contains(string, ignoreCase = true)
      DoesNotContain -> string != null && !text.contains(string, ignoreCase = true)
      Matches ->
        string != null &&
          runCatching { Regex(string, RegexOption.IGNORE_CASE).containsMatchIn(text) }
            .getOrDefault(false)
      GreaterThan,
      GreaterThanOrEquals,
      HasAnyTag,
      HasTags,
      IsApprox,
      IsBetween,
      LessThan,
      LessThanOrEquals,
      OffBudget,
      OnBudget -> false
    }
  }

  private fun JsonElement.string(): String? =
    (this as? JsonPrimitive)?.takeIf { it.isString }?.content

  // JavaScript's includes, on an array or a string
  private fun JsonElement.contains(key: String): Boolean =
    if (this is JsonArray) any { it.string() == key } else string()?.contains(key) == true

  private fun FormulaTransactions.toFilterRow() =
    FilterRow(
      date = date,
      amount = Amount(amount),
      account = account,
      accountName = account_name,
      offBudget = off_budget == true,
      payee = payee,
      payeeName = payee_name,
      importedPayee = imported_payee?.value,
      category = category,
      categoryName = category_name,
      categoryGroup = category_group,
      categoryGroupName = category_group_name,
      notes = notes,
      cleared = cleared == true,
      reconciled = reconciled == true,
      isTransfer = transfer_id != null,
      isParent = is_parent == true,
    )

  private companion object {
    val CATEGORY_FIELDS = setOf(Field.Category, Field.CategoryGroup)
    const val MIN_YEAR = 1
    const val MAX_YEAR = 9999
  }
}
