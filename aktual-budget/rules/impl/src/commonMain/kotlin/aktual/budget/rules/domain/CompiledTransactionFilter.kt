package aktual.budget.rules.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Operator
import aktual.budget.model.RecurConfig
import aktual.budget.model.occurrences
import aktual.di.BudgetScope
import aktual.log
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

/**
 * Matches transactions against a report's filter conditions, the way upstream queries them:
 * `conditionsToAQL` in packages/loot-core/src/server/transactions/transaction-rules.ts, compiled by
 * packages/loot-core/src/server/aql/compiler.ts. This differs from how rules match: negative
 * operators also match missing values, id fields are matched by name for text operators, and
 * transfer/parent conditions work.
 *
 * Invalid conditions are skipped, as are references to saved filters (conditions with a
 * `customName`), which the report spreadsheets drop.
 */
class CompiledTransactionFilter
private constructor(private val conditions: List<FilterCondition>, private val op: ConditionOp) :
  TransactionFilter {
  override val isEmpty: Boolean
    get() = conditions.isEmpty()

  override val bounds: FilterBounds by lazy {
    if (op == Or && conditions.size > 1) {
      FilterBounds()
    } else {
      conditions.fold(FilterBounds()) { bounds, condition -> condition.narrow(bounds) }
    }
  }

  override fun matches(row: FilterRow): Boolean =
    when {
      conditions.isEmpty() -> true
      op == Or -> conditions.any { it.matches(row) }
      else -> conditions.all { it.matches(row) }
    }

  companion object {
    fun compile(conditions: List<Condition>, op: ConditionOp): CompiledTransactionFilter {
      val compiled =
        conditions
          .filter { it.customName == null }
          .mapNotNull { condition ->
            try {
              FilterCondition(CompiledCondition.compile(condition, idMappings = emptyMap()))
            } catch (e: RuleValidationException) {
              log.w(e) { "Skipping invalid filter condition $condition" }
              null
            }
          }
      return CompiledTransactionFilter(compiled, op)
    }
  }
}

@Suppress("TooManyFunctions")
private class FilterCondition(private val condition: CompiledCondition) {
  private val field = condition.field
  private val op = condition.op
  private val value = condition.value

  private val text: String
    get() = (value as Text).value ?: "null"

  private val list: List<String?>
    get() = (value as TextList).values

  private val likeRegex: Regex by lazy { likePatternToRegex(normalise("%$text%")) }

  // Upstream throws on an invalid pattern, failing the whole query. Here it just doesn't match
  private val regex: Regex? by lazy { runCatching { Regex(text) }.getOrNull() }

  // extractTagsForFilter and makeTagAqlRegex in packages/loot-core/src/shared/tags.ts
  private val tagRegexes: List<Regex> by lazy {
    CompiledCondition.FILTER_TAG.findAll(text)
      .map { it.groupValues[1] }
      .distinct()
      .map { tag -> Regex("(^|[^#])" + Regex.escape("#$tag") + "([\\s#]|$)") }
      .toList()
  }

  // A recurring date only matches its first occurrences
  private val recurDates: Set<LocalDate> by lazy {
    val config = ((value as DateMatch).value as Recur).config
    config.occurrences(until = config.searchEnd()).dates.sorted().take(RECUR_DATE_BOUNDS).toSet()
  }

  fun narrow(bounds: FilterBounds): FilterBounds {
    val ids =
      when {
        op == Is -> (value as? Text)?.value?.let(::setOf)
        op == OneOf ->
          (value as? TextList)
            ?.values
            ?.takeIf { values -> values.isNotEmpty() && null !in values }
            ?.filterNotNull()
        else -> null
      }?.toSet()
    val day = ((value as? DateMatch)?.value as? Day)?.date
    val range = value as? DateRange
    return when {
      field == Account && ids != null -> bounds.copy(accounts = bounds.accounts.narrowTo(ids))
      field == Category && ids != null -> bounds.copy(categories = bounds.categories.narrowTo(ids))
      field == Payee && ids != null -> bounds.copy(payees = bounds.payees.narrowTo(ids))
      field == Date && day != null ->
        bounds.narrowDates(
          from = day.takeIf { op in AFTER },
          to = day.takeIf { op in BEFORE },
        )
      field == Date && range != null -> bounds.narrowDates(from = range.start, to = range.end)
      else -> bounds
    }
  }

  private fun FilterBounds.narrowDates(from: LocalDate?, to: LocalDate?): FilterBounds =
    copy(
      from = listOfNotNull(this.from, from).maxOrNull(),
      to = listOfNotNull(this.to, to).minOrNull(),
    )

  private fun Set<String>?.narrowTo(ids: Set<String>): Set<String> = this?.intersect(ids) ?: ids

  fun matches(row: FilterRow): Boolean =
    when (field.type) {
      FieldType.String -> matchesText(row.text())
      Id -> matchesId(row)
      FieldType.Number -> matchesAmount(row.amount.toLong().toDouble())
      Date -> matchesDate(row.date)
      FieldType.Boolean -> matchesFlag(row)
      Saved -> false
    }

  private fun FilterRow.text(): String? =
    when (field) {
      ImportedPayee -> importedPayee
      PayeeName -> payeeName
      Notes -> notes
      Payee,
      Date,
      Amount,
      Category,
      CategoryGroup,
      Account,
      Cleared,
      Reconciled,
      Saved,
      Transfer,
      Parent -> null
    }

  private fun FilterRow.id(): String? =
    when (field) {
      Payee -> payee?.value
      Category -> category?.value
      CategoryGroup -> categoryGroup?.value
      Account -> account.value
      ImportedPayee,
      PayeeName,
      Date,
      Notes,
      Amount,
      Cleared,
      Reconciled,
      Saved,
      Transfer,
      Parent -> null
    }

  private fun FilterRow.name(): String? =
    when (field) {
      Payee -> payeeName
      Category -> categoryName
      CategoryGroup -> categoryGroupName
      Account -> accountName
      ImportedPayee,
      PayeeName,
      Date,
      Notes,
      Amount,
      Cleared,
      Reconciled,
      Saved,
      Transfer,
      Parent -> null
    }

  // String fields are compared lowercased, except by tag operators
  @Suppress("CyclomaticComplexMethod")
  private fun matchesText(fieldValue: String?): Boolean {
    val lower = fieldValue?.lowercase()
    return when (op) {
      Is -> if (text.isEmpty()) fieldValue.isNullOrEmpty() else lower == text
      IsNot -> lower == null || lower != text
      Contains -> like(fieldValue)
      DoesNotContain -> fieldValue == null || !like(fieldValue)
      Matches -> regex?.containsMatchIn(lower.orEmpty()) == true
      OneOf -> lower in list
      NotOneOf -> list.isNotEmpty() && list.all { lower.isNot(it) }
      HasTags ->
        tagRegexes.isNotEmpty() && tagRegexes.all { it.containsMatchIn(fieldValue.orEmpty()) }
      HasAnyTag ->
        tagRegexes.isNotEmpty() && tagRegexes.any { it.containsMatchIn(fieldValue.orEmpty()) }

      GreaterThan,
      GreaterThanOrEquals,
      IsApprox,
      IsBetween,
      LessThan,
      LessThanOrEquals,
      OffBudget,
      OnBudget -> false
    }
  }

  // Ids are compared as they are. Text operators match the name of what the id points to
  @Suppress("CyclomaticComplexMethod")
  private fun matchesId(row: FilterRow): Boolean {
    val id = row.id()
    return when (op) {
      Is -> matchesIdSpecialCases(row) && id == (value as Text).value
      IsNot -> matchesIdSpecialCases(row) && id.isNot((value as Text).value)
      OneOf -> id in list
      NotOneOf -> list.isNotEmpty() && list.all { id.isNot(it) }
      Contains -> like(row.name())
      DoesNotContain -> row.name() == null || !like(row.name())
      Matches -> regex?.containsMatchIn(row.name().orEmpty()) == true
      OnBudget -> !row.offBudget
      OffBudget -> row.offBudget

      GreaterThan,
      GreaterThanOrEquals,
      HasAnyTag,
      HasTags,
      IsApprox,
      IsBetween,
      LessThan,
      LessThanOrEquals -> false
    }
  }

  // AQL's $ne: a missing value matches anything but null
  private fun String?.isNot(other: String?): Boolean =
    if (other == null) this != null else this == null || this != other

  // conditionSpecialCases in transaction-rules.ts: "category is nothing" leaves out transfers and
  // split parents, "category is not nothing" leaves out split parents
  private fun matchesIdSpecialCases(row: FilterRow): Boolean {
    if (field != Category || (value as Text).value != null) return true
    return when (op) {
      Is -> !row.isTransfer && !row.isParent
      IsNot -> !row.isParent
      Contains,
      DoesNotContain,
      GreaterThan,
      GreaterThanOrEquals,
      HasAnyTag,
      HasTags,
      IsApprox,
      IsBetween,
      LessThan,
      LessThanOrEquals,
      Matches,
      NotOneOf,
      OffBudget,
      OnBudget,
      OneOf -> true
    }
  }

  // Inflow and outflow only match money going that way, with outflows compared as positive
  private fun matchesAmount(amount: Double): Boolean {
    val value = value
    if (value is Between) {
      return op == IsBetween &&
        amount in minOf(value.num1, value.num2)..maxOf(value.num1, value.num2)
    }

    val number = (value as ConditionValue.Number).value
    val options = condition.options
    val fieldValue =
      when {
        options?.outflow == true -> if (amount < 0) -amount else return false
        options?.inflow == true -> if (amount > 0) amount else return false
        else -> amount
      }

    return when (op) {
      Is -> fieldValue == number
      IsApprox ->
        CompiledCondition.approxNumberThreshold(number).let {
          fieldValue in number - it..number + it
        }
      GreaterThan -> fieldValue > number
      GreaterThanOrEquals -> fieldValue >= number
      LessThan -> fieldValue < number
      LessThanOrEquals -> fieldValue <= number

      Contains,
      DoesNotContain,
      HasAnyTag,
      HasTags,
      IsBetween,
      IsNot,
      Matches,
      NotOneOf,
      OffBudget,
      OnBudget,
      OneOf -> false
    }
  }

  @Suppress("CyclomaticComplexMethod")
  private fun matchesDate(date: LocalDate): Boolean {
    val value = value
    if (value is DateRange) return op == IsBetween && date in value.start..value.end
    val match = (value as DateMatch).value
    return when (op) {
      Is ->
        when (match) {
          is Day -> date == match.date
          is Month -> date.yearMonth == match.month
          is Year -> date.year == match.year
          is Recur -> date in recurDates
        }

      IsApprox ->
        when (match) {
          is Day -> date in match.date.minus(APPROX_DAYS)..match.date.plus(APPROX_DAYS)
          is Recur -> recurDates.any { date in it.minus(APPROX_DAYS)..it.plus(APPROX_DAYS) }
          is Month,
          is Year -> false
        }

      GreaterThan -> compareDate(date, match)?.let { it > 0 } == true
      GreaterThanOrEquals -> compareDate(date, match)?.let { it >= 0 } == true
      LessThan -> compareDate(date, match)?.let { it < 0 } == true
      LessThanOrEquals -> compareDate(date, match)?.let { it <= 0 } == true

      Contains,
      DoesNotContain,
      HasAnyTag,
      HasTags,
      IsBetween,
      IsNot,
      Matches,
      NotOneOf,
      OffBudget,
      OnBudget,
      OneOf -> false
    }
  }

  // Upstream compares dates as strings, so a month or year is before every day in it. A recurring
  // date has no single date to compare against, so matches nothing
  private fun compareDate(date: LocalDate, value: DateValue): Int? =
    when (value) {
      is Day -> date.toString().compareTo(value.date.toString())
      is Month -> date.toString().compareTo(value.month.toString())
      is Year -> date.toString().compareTo(value.year.toString())
      is Recur -> null
    }

  private fun matchesFlag(row: FilterRow): Boolean {
    val flag =
      when (field) {
        Cleared -> row.cleared
        Reconciled -> row.reconciled
        Transfer -> row.isTransfer
        Parent -> row.isParent
        ImportedPayee,
        Payee,
        PayeeName,
        Date,
        Notes,
        Amount,
        Category,
        CategoryGroup,
        Account,
        Saved -> return false
      }
    return op == Is && flag == (value as Flag).value
  }

  // UNICODE_LIKE(pattern, NORMALISE(value)), where a missing value is an empty string
  private fun like(fieldValue: String?): Boolean =
    likeRegex.containsMatchIn(fieldValue?.let(::normalise).orEmpty())

  private fun RecurConfig.searchEnd(): LocalDate {
    val periods = RECUR_SEARCH_PERIODS * (interval ?: 1).coerceAtLeast(1)
    return when (frequency) {
      Daily -> start.plus(periods, DAY)
      Weekly -> start.plus(periods, WEEK)
      Monthly -> start.plus(periods, MONTH)
      Yearly,
      Unknown -> start.plus(periods, YEAR)
    }
  }

  private companion object {
    // conditionsToAQL's recurDateBounds
    const val RECUR_DATE_BOUNDS = 100

    // Enough periods to find RECUR_DATE_BOUNDS occurrences, since some periods have none
    const val RECUR_SEARCH_PERIODS = 2 * RECUR_DATE_BOUNDS

    val APPROX_DAYS = DatePeriod(days = 2)
    val AFTER = setOf<Operator>(Is, GreaterThan, GreaterThanOrEquals)
    val BEFORE = setOf<Operator>(Is, LessThan, LessThanOrEquals)
  }
}

@Inject
@ContributesBinding(BudgetScope::class)
class TransactionFilterCompilerImpl : TransactionFilterCompiler {
  override fun compile(conditions: List<Condition>, op: ConditionOp): TransactionFilter =
    CompiledTransactionFilter.compile(conditions, op)
}
