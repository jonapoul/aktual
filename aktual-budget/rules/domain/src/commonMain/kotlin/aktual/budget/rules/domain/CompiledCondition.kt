@file:Suppress("BracesOnWhenStatements", "UnnecessaryParentheses")

package aktual.budget.rules.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOptions
import aktual.budget.model.Operator
import aktual.budget.model.RecurConfig
import aktual.budget.model.occurrences
import aktual.budget.model.tagsInNotes
import kotlin.math.abs
import kotlin.math.floor
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * A validated rule condition. Port of `Condition` in
 * packages/loot-core/src/server/rules/condition.ts: the constructor's validation is [compile], and
 * [eval] is `Condition.eval`.
 */
internal class CompiledCondition
private constructor(
  val field: RuleField,
  val op: Operator,
  val value: ConditionValue,
  private val options: ConditionOptions?,
) {
  // Matches compiles the pattern on every eval upstream and treats an invalid one as no match
  private val regex: Regex? by lazy {
    (value as? Text)?.let { runCatching { Regex(it.value ?: "null") }.getOrNull() }
  }

  // extractTagsForFilter in packages/loot-core/src/shared/tags.ts, lowercased for comparison
  private val tags: List<String> by lazy {
    val text = (value as? Text)?.value.orEmpty()
    FILTER_TAG.findAll(text).map { it.groupValues[1].lowercase() }.distinct().toList()
  }

  @Suppress("CyclomaticComplexMethod")
  fun eval(subject: RuleSubject): Boolean {
    val transaction = subject.transaction
    return when (field.type) {
      FieldType.String -> {
        val text =
          when (field) {
            ImportedPayee -> transaction.importedPayee
            PayeeName -> subject.payeeName
            Account,
            Amount,
            Category,
            CategoryGroup,
            Cleared,
            Date,
            Notes,
            Parent,
            Payee,
            Reconciled,
            Saved,
            Transfer -> transaction.notes
          }
        // For strings, a missing value counts as an empty string
        evalText(text.orEmpty().lowercase(), subject)
      }

      FieldType.Id -> {
        val id =
          when (field) {
            Payee -> transaction.payee?.value
            Category -> transaction.category?.value
            Account -> transaction.account.value
            Amount,
            CategoryGroup,
            Cleared,
            Date,
            ImportedPayee,
            Notes,
            Parent,
            PayeeName,
            Reconciled,
            Saved,
            Transfer -> subject.categoryGroup?.value ?: return false
          }
        evalText(id?.lowercase(), subject)
      }

      FieldType.Number -> evalNumber(transaction.amount.toLong().toDouble())

      FieldType.Date -> evalDate(transaction.date)

      FieldType.Boolean -> {
        val flag =
          when (field) {
            Cleared -> transaction.cleared
            Reconciled -> transaction.reconciled
            // Upstream reads `transfer` and `parent` straight off the transaction, which has
            // neither field while rules run, so conditions on them never match
            ImportedPayee,
            Payee,
            PayeeName,
            Date,
            Notes,
            Amount,
            Category,
            CategoryGroup,
            Account,
            Saved,
            Transfer,
            Parent -> return false
          }
        op == Is && flag == (value as Flag).value
      }

      FieldType.Saved -> false
    }
  }

  @Suppress("CyclomaticComplexMethod")
  private fun evalText(fieldValue: String?, subject: RuleSubject): Boolean =
    when (op) {
      Is -> fieldValue == (value as Text).value
      IsNot -> fieldValue != (value as Text).value
      Contains -> fieldValue != null && fieldValue.contains(textValue())
      DoesNotContain -> fieldValue != null && !fieldValue.contains(textValue())
      OneOf -> fieldValue != null && fieldValue in (value as TextList).values
      NotOneOf -> fieldValue != null && fieldValue !in (value as TextList).values
      Matches -> fieldValue != null && regex?.containsMatchIn(fieldValue) == true
      HasTags -> fieldValue != null && tagsInNotes(fieldValue).containsAll(tags)
      HasAnyTag -> fieldValue != null && tags.any { it in tagsInNotes(fieldValue) }
      OnBudget -> subject.account?.offBudget == false
      OffBudget -> subject.account?.offBudget == true

      GreaterThan,
      GreaterThanOrEquals,
      IsApprox,
      IsBetween,
      LessThan,
      LessThanOrEquals -> false
    }

  // JS's String(value) of a null condition value is "null"
  private fun textValue(): String = (value as Text).value ?: "null"

  private fun evalNumber(amount: Double): Boolean {
    var fieldValue = amount
    if (options?.outflow == true) {
      if (fieldValue > 0) return false
      fieldValue = -fieldValue
    } else if (options?.inflow == true) {
      if (fieldValue < 0) return false
    }

    return when (val value = value) {
      is Between -> {
        val low = minOf(value.num1, value.num2)
        val high = maxOf(value.num1, value.num2)
        op == IsBetween && fieldValue in low..high
      }

      is ConditionValue.Number -> {
        val number = value.value
        when (op) {
          Is -> fieldValue == number
          IsApprox -> {
            val threshold = approxNumberThreshold(number)
            fieldValue in (number - threshold)..(number + threshold)
          }
          GreaterThan -> fieldValue > number
          GreaterThanOrEquals -> fieldValue >= number
          LessThan -> fieldValue < number
          LessThanOrEquals -> fieldValue <= number

          Contains,
          DoesNotContain,
          IsNot,
          Matches,
          NotOneOf,
          OffBudget,
          OnBudget,
          OneOf,
          IsBetween,
          HasAnyTag,
          HasTags -> false
        }
      }

      else -> false
    }
  }

  private fun evalDate(date: LocalDate): Boolean {
    val value = (value as DateMatch).value
    return when (op) {
      Is ->
        when (value) {
          is Day -> date == value.date
          is Month -> date.yearMonth == value.month
          is Year -> date.year == value.year
          is Recur -> value.config.occursOn(date)
        }

      IsApprox ->
        when (value) {
          is Day -> date in value.date.minus(APPROX_DAYS)..value.date.plus(APPROX_DAYS)
          is Recur -> value.config.occursBetween(date.minus(APPROX_DAYS), date.plus(APPROX_DAYS))
          is Month,
          is Year -> false
        }

      GreaterThan -> date > (value as Day).date
      GreaterThanOrEquals -> date >= (value as Day).date
      LessThan -> date < (value as Day).date
      LessThanOrEquals -> date <= (value as Day).date

      Contains,
      DoesNotContain,
      IsNot,
      Matches,
      NotOneOf,
      OffBudget,
      OnBudget,
      OneOf,
      IsBetween,
      HasAnyTag,
      HasTags -> false
    }
  }

  companion object {
    private val APPROX_DAYS = DatePeriod(days = 2)
    private val FILTER_TAG = Regex("""#*([^#\s]+)""")
    private val RecurJson = Json { ignoreUnknownKeys = true }
    private val ID_MAPPED_OPS = setOf<Operator>(Is, IsNot, OneOf, NotOneOf)
    private val NON_NULLABLE_TYPES = setOf(FieldType.Date, FieldType.Number, FieldType.Boolean)

    private val NON_EMPTY_OPS =
      setOf<Operator>(Contains, Matches, DoesNotContain, HasTags, HasAnyTag)

    /**
     * Validates [condition] the way upstream's `Condition` constructor does, throwing
     * [RuleValidationException] if it's invalid. Ids in `is`/`isNot`/`oneOf`/`notOneOf` conditions
     * on id fields are swapped for their targets in [idMappings] (`migrateIds` in
     * packages/loot-core/src/server/rules/rule-utils.ts).
     */
    fun compile(condition: Condition, idMappings: Map<String, String>): CompiledCondition {
      val field = condition.field.toRuleField()
      ruleAssert(field != null) { "Invalid condition field: ${condition.field}" }
      val op = condition.operator
      ruleAssert(field.isValidOp(op)) { "Invalid condition operator: $op (field: $field)" }

      val raw = condition.value
      if (field.type in NON_NULLABLE_TYPES) {
        ruleAssert(raw != JsonNull) { "Field cannot be empty: $field" }
      }

      val parsed =
        when (field.type) {
          FieldType.Date -> parseDate(op, raw, field)
          FieldType.Id -> parseId(op, raw, field, idMappings)
          FieldType.String -> parseString(op, raw, field)
          FieldType.Number -> parseNumber(op, raw, field)
          FieldType.Boolean -> parseBoolean(raw, field)
          FieldType.Saved -> throw RuleValidationException("Invalid condition field: $field")
        }
      return CompiledCondition(field, op, parsed, condition.options)
    }

    private fun parseDate(op: Operator, raw: JsonElement, field: RuleField): ConditionValue {
      val parsed =
        when {
          raw.isJsString() -> parseDateString((raw as JsonPrimitive).content)
          raw is JsonObject && raw["frequency"].let { it != null && it != JsonNull } ->
            parseRecurDate(raw)
          else -> null
        }
      ruleAssert(parsed != null) { "Invalid date format (field: $field)" }

      // Approximate only works with exact & recurring dates, comparisons only with exact dates
      when (op) {
        IsApprox ->
          ruleAssert(parsed is Day || parsed is Recur) {
            "Invalid date value for \"isapprox\" (field: $field)"
          }

        GreaterThan,
        GreaterThanOrEquals,
        LessThan,
        LessThanOrEquals ->
          ruleAssert(parsed is Day) { "Invalid date value for \"$op\" (field: $field)" }

        Contains,
        DoesNotContain,
        Is,
        IsNot,
        Matches,
        NotOneOf,
        OffBudget,
        OnBudget,
        OneOf,
        IsBetween,
        HasAnyTag,
        HasTags -> Unit
      }
      return ConditionValue.DateMatch(parsed)
    }

    private fun parseId(
      op: Operator,
      raw: JsonElement,
      field: RuleField,
      idMappings: Map<String, String>,
    ): ConditionValue {
      val mapped = op in ID_MAPPED_OPS
      fun String?.mapped(): String? = if (mapped && this != null) idMappings[this] ?: this else this

      if (op == OneOf || op == NotOneOf) {
        ruleAssert(raw is JsonArray) { "oneOf must have an array value (field: $field)" }
        return ConditionValue.TextList(raw.map { it.scalarOrNull().mapped() }.distinct())
      }
      return ConditionValue.Text(raw.scalarOrNull().mapped())
    }

    private fun parseString(op: Operator, raw: JsonElement, field: RuleField): ConditionValue {
      if (op == OneOf || op == NotOneOf) {
        ruleAssert(raw is JsonArray) { "oneOf must have an array value (field: $field): $raw" }
        // JS's filter(Boolean) drops nulls and empty strings
        val values =
          raw
            .asSequence()
            .mapNotNull { it.scalarOrNull() }
            .filter { it.isNotEmpty() }
            .map { it.lowercase() }
            .toList()
        return ConditionValue.TextList(values)
      }

      ruleAssert(raw.isJsString()) { "Invalid string value (field: $field)" }
      val text = (raw as JsonPrimitive).content
      if (op in NON_EMPTY_OPS) {
        ruleAssert(text.isNotEmpty()) { "$op must have non-empty string (field: $field)" }
      }

      // Upstream lowercases everything but tags, including the regex of a matches condition
      return ConditionValue.Text(if (op == HasTags || op == HasAnyTag) text else text.lowercase())
    }

    private fun parseNumber(op: Operator, raw: JsonElement, field: RuleField): ConditionValue {
      val parsed =
        if (raw is JsonObject) {
          parseBetween(raw)
        } else {
          raw.numberOrNull()?.let(ConditionValue::Number)
        }
      ruleAssert(parsed != null) {
        "Value must be a number or between amount: $raw (field: $field)"
      }

      if (op == IsBetween) {
        ruleAssert(parsed is Between) {
          "Invalid between value for \"$op\" (field: $field)"
        }
      } else {
        ruleAssert(parsed is ConditionValue.Number) {
          "Invalid number value for \"$op\" (field: $field)"
        }
      }
      return parsed
    }

    private fun parseBetween(raw: JsonObject): ConditionValue.Between? {
      val num1 = raw["num1"].numberOrNull() ?: return null
      val num2 = raw["num2"].numberOrNull() ?: return null
      return ConditionValue.Between(num1, num2)
    }

    private fun parseBoolean(raw: JsonElement, field: RuleField): ConditionValue {
      val flag = (raw as? JsonPrimitive)?.takeIf { !it.isString }?.booleanOrNull
      ruleAssert(flag != null) { "Value must be a boolean: $raw (field: $field)" }
      return ConditionValue.Flag(flag)
    }

    // parseDateString in packages/loot-core/src/server/rules/rule-utils.ts
    internal fun parseDateString(text: String): DateValue? =
      when (text.length) {
        DAY_LENGTH -> parseLocalDate(text)?.let(DateValue::Day)
        MONTH_LENGTH -> parseLocalDate("$text-01")?.let { DateValue.Month(it.yearMonth) }
        YEAR_LENGTH -> parseLocalDate("$text-01-01")?.let { DateValue.Year(it.year) }
        else -> null
      }

    private fun parseLocalDate(text: String): LocalDate? = runCatching {
      LocalDate.parse(text)
    }
      .getOrNull()

    // parseRecurDate in rule-utils.ts, via recurConfigToRSchedule in
    // packages/loot-core/src/shared/schedules.ts which rejects unknown frequencies
    private fun parseRecurDate(raw: JsonObject): DateValue? {
      val config =
        try {
          RecurJson.decodeFromJsonElement(RecurConfig.serializer(), raw)
        } catch (e: IllegalArgumentException) {
          throw RuleValidationException("Invalid recurring date", e)
        }
      ruleAssert(config.frequency != Unknown) { "Invalid recurring date config" }
      return DateValue.Recur(config)
    }

    // getApproxNumberThreshold in packages/loot-core/src/shared/rules.ts
    @Suppress("MagicNumber")
    private fun approxNumberThreshold(number: Double): Double =
      jsRound(abs(number) * 0.075).toDouble()
  }
}

internal sealed interface ConditionValue {
  // An id or string. Strings are lowercased, except for tag conditions
  data class Text(val value: String?) : ConditionValue

  data class TextList(val values: List<String?>) : ConditionValue

  data class Number(val value: Double) : ConditionValue

  data class Between(val num1: Double, val num2: Double) : ConditionValue

  data class Flag(val value: Boolean) : ConditionValue

  data class DateMatch(val value: DateValue) : ConditionValue
}

internal sealed interface DateValue {
  data class Day(val date: LocalDate) : DateValue

  data class Month(val month: YearMonth) : DateValue

  data class Year(val year: Int) : DateValue

  data class Recur(val config: RecurConfig) : DateValue
}

// rSchedule's occursOn, which ignores the weekend skipping that only affects the next date shown
private fun RecurConfig.occursOn(date: LocalDate): Boolean = date in occurrences(until = date).dates

// rSchedule's occursBetween, inclusive at both ends
private fun RecurConfig.occursBetween(start: LocalDate, end: LocalDate): Boolean =
  occurrences(until = end).dates.any { it in start..end }

// JS's Math.round, which rounds halves up rather than away from zero
internal fun jsRound(value: Double): Long = floor(value + JS_ROUND_HALF).toLong()

private const val JS_ROUND_HALF = 0.5

private fun JsonElement.isJsString(): Boolean = this is JsonPrimitive && isString

// A JSON primitive's content, as JS would compare it against a transaction's id
private fun JsonElement.scalarOrNull(): String? =
  if (this is JsonPrimitive) contentOrNull else toString()

private fun JsonElement?.numberOrNull(): Double? =
  (this as? JsonPrimitive)?.takeIf { !it.isString }?.doubleOrNull

private const val DAY_LENGTH = 10
private const val MONTH_LENGTH = 7
private const val YEAR_LENGTH = 4
