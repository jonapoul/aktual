package aktual.budget.rules.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.model.occurrences
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import logcat.logcat

/** A transaction as a report filter sees it, with the names that id conditions can match */
data class FilterRow(
  val date: LocalDate,
  val amount: Amount,
  val account: AccountId,
  val accountName: String? = null,
  val offBudget: Boolean = false,
  val payee: PayeeId? = null,
  val payeeName: String? = null,
  val importedPayee: String? = null,
  val category: CategoryId? = null,
  val categoryName: String? = null,
  val categoryGroup: CategoryGroupId? = null,
  val categoryGroupName: String? = null,
  val notes: String? = null,
  val cleared: Boolean = false,
  val reconciled: Boolean = false,
  val isTransfer: Boolean = false,
  val isParent: Boolean = false,
)

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
class TransactionFilter
private constructor(private val conditions: List<FilterCondition>, private val op: ConditionOp) {
  /** True when there's nothing to filter by, so every transaction matches */
  val isEmpty: Boolean
    get() = conditions.isEmpty()

  fun matches(row: FilterRow): Boolean =
    when {
      conditions.isEmpty() -> true
      op == Or -> conditions.any { it.matches(row) }
      else -> conditions.all { it.matches(row) }
    }

  companion object {
    fun compile(conditions: List<Condition>, op: ConditionOp): TransactionFilter {
      val compiled =
        conditions
          .filter { it.customName == null }
          .mapNotNull { condition ->
            try {
              FilterCondition(CompiledCondition.compile(condition, idMappings = emptyMap()))
            } catch (e: RuleValidationException) {
              logcat.w(e) { "Skipping invalid filter condition $condition" }
              null
            }
          }
      return TransactionFilter(compiled, op)
    }
  }
}

@Suppress("TooManyFunctions")
private class FilterCondition(private val condition: CompiledCondition) {
  private val field = condition.field
  private val op = condition.op
  private val value = condition.value

  private val text: String
    get() = (value as ConditionValue.Text).value ?: "null"

  private val list: List<String?>
    get() = (value as ConditionValue.TextList).values

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
    val config = ((value as ConditionValue.DateMatch).value as DateValue.Recur).config
    config.occurrences(until = config.searchEnd()).dates.sorted().take(RECUR_DATE_BOUNDS).toSet()
  }

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
      RuleField.Amount,
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
      RuleField.Amount,
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
      RuleField.Amount,
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
      Is -> matchesIdSpecialCases(row) && id == (value as ConditionValue.Text).value
      IsNot -> matchesIdSpecialCases(row) && id.isNot((value as ConditionValue.Text).value)
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
    if (field != Category || (value as ConditionValue.Text).value != null) return true
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
    val value = (value as ConditionValue.DateMatch).value
    return when (op) {
      Is ->
        when (value) {
          is Day -> date == value.date
          is Month -> date.yearMonth == value.month
          is Year -> date.year == value.year
          is Recur -> date in recurDates
        }

      IsApprox ->
        when (value) {
          is Day -> date in value.date.minus(APPROX_DAYS)..value.date.plus(APPROX_DAYS)
          is Recur -> recurDates.any { date in it.minus(APPROX_DAYS)..it.plus(APPROX_DAYS) }
          is Month,
          is Year -> false
        }

      GreaterThan -> compareDate(date, value)?.let { it > 0 } == true
      GreaterThanOrEquals -> compareDate(date, value)?.let { it >= 0 } == true
      LessThan -> compareDate(date, value)?.let { it < 0 } == true
      LessThanOrEquals -> compareDate(date, value)?.let { it <= 0 } == true

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
        RuleField.Amount,
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
      Daily -> start.plus(periods, DateTimeUnit.DAY)
      Weekly -> start.plus(periods, DateTimeUnit.WEEK)
      Monthly -> start.plus(periods, DateTimeUnit.MONTH)
      Yearly,
      Unknown -> start.plus(periods, DateTimeUnit.YEAR)
    }
  }

  private companion object {
    // conditionsToAQL's recurDateBounds
    const val RECUR_DATE_BOUNDS = 100

    // Enough periods to find RECUR_DATE_BOUNDS occurrences, since some periods have none
    const val RECUR_SEARCH_PERIODS = 2 * RECUR_DATE_BOUNDS

    val APPROX_DAYS = DatePeriod(days = 2)
  }
}

// likePatternToRegex in packages/loot-core/src/platform/server/sqlite/unicodeLike.ts: % is any
// run of characters, ? is any one character, and a backslash escapes either
@Suppress("UnnecessaryParentheses")
internal fun likePatternToRegex(pattern: String): Regex {
  val regex = StringBuilder()
  var i = 0
  while (i < pattern.length) {
    val ch = pattern[i]
    val next = pattern.getOrNull(i + 1)
    when (ch) {
      '\\' if (next == '%' || next == '?' || next == '\\') -> {
        regex.append(Regex.escape(next.toString()))
        i++
      }
      '%' -> {
        regex.append(".*")
      }
      '?' -> {
        regex.append('.')
      }
      else -> {
        regex.append(Regex.escape(ch.toString()))
      }
    }
    i++
  }
  return Regex(regex.toString(), RegexOption.IGNORE_CASE)
}

// getNormalisedString in packages/loot-core/src/shared/normalisation.ts
internal fun normalise(value: String): String {
  var lower = value.lowercase()
  for ((from, to) in LETTER_REPLACEMENTS) lower = lower.replace(from, to)
  return stripDiacritics(lower)
}

private val LETTER_REPLACEMENTS = listOf("ł" to "l", "ø" to "o", "ß" to "ss", "œ" to "oe")

/** Decomposes [value] and drops the combining marks, so "é" becomes "e" */
internal expect fun stripDiacritics(value: String): String
