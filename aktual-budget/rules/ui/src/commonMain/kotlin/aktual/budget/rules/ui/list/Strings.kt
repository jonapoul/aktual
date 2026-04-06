@file:Suppress("MagicNumber")

package aktual.budget.rules.ui.list

import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOptions
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.Field
import aktual.budget.model.NumberFormatConfig
import aktual.budget.model.Operator
import aktual.budget.model.RecurConfig
import aktual.budget.model.RecurEndMode
import aktual.budget.model.RecurFrequency
import aktual.budget.model.RecurPattern
import aktual.budget.model.RecurType
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleStage
import aktual.budget.model.ScheduleId
import aktual.budget.model.WeekendSolveMode
import aktual.budget.model.isIdField
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.ui.AktualTypography
import aktual.core.ui.LocalCurrencyConfig
import aktual.core.ui.LocalDateFormatter
import aktual.core.ui.LocalNumberFormatConfig
import aktual.core.ui.LocalPrivacyEnabled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

@Composable
internal fun RuleStage.string(): String =
  when (this) {
    RuleStage.Pre -> Strings.rulesStagePre
    RuleStage.Default -> Strings.rulesStageNone
    RuleStage.Post -> Strings.rulesStagePost
  }

@Composable
internal fun headerText(onAction: (ListRulesAction) -> Unit): AnnotatedString {
  val headerText = Strings.rulesHeaderText
  val learnMoreText = Strings.rulesHeaderLearnMore
  val theme = LocalTheme.current
  val defaultStyle = AktualTypography.bodyMedium.copy(color = theme.pageTextLight)
  val linkStyle = defaultStyle.copy(color = theme.pageTextLinkLight, textDecoration = Underline)
  return remember(headerText, learnMoreText, defaultStyle, linkStyle) {
    buildAnnotatedString {
      withStyle(defaultStyle.toSpanStyle()) {
        append(headerText)
        append(" ")
      }
      withLink(
        LinkAnnotation.Clickable(
          tag = LEARN_MORE_URL,
          styles = TextLinkStyles(linkStyle.toSpanStyle()),
          linkInteractionListener = { onAction(OpenUrl(LEARN_MORE_URL)) },
        )
      ) {
        append(learnMoreText)
      }
    }
  }
}

private const val LEARN_MORE_URL = "https://actualbudget.org/docs/budgeting/rules/"

private fun JsonArray.toList(): List<String> = map { it.jsonPrimitive.content }

@Composable
@Suppress("ElseCaseInsteadOfExhaustiveWhen")
internal fun rememberConditionText(
  prefix: String,
  condition: Condition,
  styles: RuleSpanStyles,
): AnnotatedString {
  val fieldText = condition.field.string(condition.options)
  val opText = condition.operator.displayString()

  val dateFormat = LocalDateFormatter.current
  val numberFormat = LocalNumberFormatConfig.current
  val currency = LocalCurrencyConfig.current
  val privacy = LocalPrivacyEnabled.current

  val nameFetcher = LocalNameFetcher.current
  val fieldNamesFlow =
    remember(nameFetcher, condition) {
      when (condition.field) {
        Field.Acct,
        Field.Account,
        Field.Category,
        Field.CategoryGroup,
        Field.Description,
        Field.Payee ->
          when (val value = condition.value) {
            is JsonArray -> nameFetcher.names(condition.field, value.toList())
            is JsonPrimitive ->
              nameFetcher.name(condition.field, value.content).filterNotNull().map(::JsonPrimitive)
            else -> flowOf(null)
          }
        else -> flowOf(null)
      }
    }

  val fieldNames by fieldNamesFlow.collectAsStateWithLifecycle(initialValue = null)

  return remember(
    condition,
    styles,
    opText,
    fieldText,
    fieldNames,
    dateFormat,
    numberFormat,
    currency,
    privacy,
  ) {
    buildAnnotatedString {
      withStyle(styles.default) { append(prefix) }
      withStyle(styles.highlighted) { append(fieldText) }
      withStyle(styles.default) {
        append(" ")
        append(opText)
        append(" ")
      }
      when (val value = fieldNames ?: condition.value) {
        is JsonPrimitive -> {
          withStyle(styles.highlighted) {
            if (condition.field == Field.Amount) {
              append(
                Amount(value.int)
                  .toString(
                    numberFormatConfig = numberFormat,
                    currencyConfig = currency,
                    includeSign = true,
                    isPrivacyEnabled = privacy,
                  )
              )
            } else {
              append(value.content)
            }
          }
        }

        is JsonArray -> {
          withStyle(styles.default) { append("[") }
          value.forEachIndexed { index, element ->
            withStyle(styles.highlighted) { append(element.jsonPrimitive.content) }
            if (index != value.lastIndex) {
              withStyle(styles.default) { append(", ") }
            }
          }
          withStyle(styles.default) { append("]") }
        }

        JsonNull -> {
          error("Should never see null in a condition's value: $condition")
        }

        is JsonObject -> {
          if (condition.field != Field.Date) {
            error("Should only see a JSON object in a condition value for a date: $condition")
          }
          val recurConfig = Json.decodeFromJsonElement(RecurConfig.serializer(), value)
          withStyle(styles.highlighted) { append(recurConfig.string(dateFormat)) }
        }
      }
    }
  }
}

@Composable
@Suppress("ElseCaseInsteadOfExhaustiveWhen")
internal fun rememberActionText(action: RuleAction, styles: RuleSpanStyles): AnnotatedString {
  val opText = action.opString()
  val fieldText = action.field?.string(options = null)
  val setToText = if (action.op == RuleAction.Op.Set) Strings.rulesActionSetTo else null

  val dateFormat = LocalDateFormatter.current
  val numberFormat = LocalNumberFormatConfig.current
  val currency = LocalCurrencyConfig.current
  val privacy = LocalPrivacyEnabled.current

  val nameFetcher = LocalNameFetcher.current
  val fieldNameFlow =
    remember(nameFetcher, action) {
      when (action.op) {
        RuleAction.Op.Set -> {
          val field = action.field
          if (field.isIdField()) {
            nameFetcher.name(field, action.value.content)
          } else {
            flowOf(null)
          }
        }

        RuleAction.Op.LinkSchedule -> {
          nameFetcher.name(ScheduleId(action.value.content))
        }

        else -> {
          flowOf(null)
        }
      }
    }

  val fieldName by fieldNameFlow.collectAsStateWithLifecycle(initialValue = "...")

  return remember(
    action,
    styles,
    opText,
    fieldText,
    setToText,
    fieldName,
    dateFormat,
    numberFormat,
    currency,
    privacy,
  ) {
    buildAnnotatedString {
      when (action.op) {
        RuleAction.Op.AppendNotes -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) { append(action.value.content) }
        }
        RuleAction.Op.DeleteTransaction -> {
          withStyle(styles.default) { append(opText) }
        }
        RuleAction.Op.LinkSchedule -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) { append(fieldName) }
        }
        RuleAction.Op.PrependNotes -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) { append(action.value.content) }
        }
        RuleAction.Op.Set -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) { append(fieldText) }
          withStyle(styles.default) { append(setToText) }
          withStyle(styles.highlighted) {
            if (action.field == Field.Amount) {
              append(formatAmount(action, numberFormat, currency, privacy))
            } else if (fieldName != null) {
              append(fieldName)
            } else {
              append(action.value.content)
            }
          }
        }
        RuleAction.Op.SetSplitAmount -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) {
            append(formatAmount(action, numberFormat, currency, privacy))
          }
        }
      }
    }
  }
}

private fun formatAmount(
  action: RuleAction,
  numberFormat: NumberFormatConfig,
  currency: CurrencyConfig,
  privacy: Boolean,
): String =
  Amount(action.value.content.toInt())
    .toString(
      numberFormatConfig = numberFormat,
      currencyConfig = currency,
      includeSign = true,
      isPrivacyEnabled = privacy,
    )

@Composable
private fun RuleAction.opString(): String =
  when (op) {
    RuleAction.Op.AppendNotes -> Strings.rulesOpAppendNotes
    RuleAction.Op.DeleteTransaction -> Strings.rulesOpDeleteTransaction
    RuleAction.Op.LinkSchedule -> Strings.rulesOpLinkSchedule
    RuleAction.Op.PrependNotes -> Strings.rulesOpPrependNotes
    RuleAction.Op.Set -> Strings.rulesOpSet
    RuleAction.Op.SetSplitAmount -> Strings.rulesOpSetSplitAmount
  }

@Composable
private fun Operator.displayString(): String =
  when (this) {
    Operator.Contains -> Strings.rulesOperatorContains
    Operator.DoesNotContain -> Strings.rulesOperatorDoesNotContain
    Operator.GreaterThan -> Strings.rulesOperatorGreaterThan
    Operator.GreaterThanOrEquals -> Strings.rulesOperatorGreaterThanOrEquals
    Operator.HasTags -> Strings.rulesOperatorHasTags
    Operator.Is -> Strings.rulesOperatorIs
    Operator.IsApprox -> Strings.rulesOperatorIsApprox
    Operator.IsBetween -> Strings.rulesOperatorIsBetween
    Operator.IsNot -> Strings.rulesOperatorIsNot
    Operator.LessThan -> Strings.rulesOperatorLessThan
    Operator.LessThanOrEquals -> Strings.rulesOperatorLessThanOrEquals
    Operator.Matches -> Strings.rulesOperatorMatches
    Operator.NotOneOf -> Strings.rulesOperatorNotOneOf
    Operator.OffBudget -> Strings.rulesOperatorOffBudget
    Operator.OnBudget -> Strings.rulesOperatorOnBudget
    Operator.OneOf -> Strings.rulesOperatorOneOf
  }

@Composable
private fun Field.string(options: ConditionOptions?): String =
  when (this) {
    Field.Account,
    Field.Acct -> Strings.rulesFieldAccount
    Field.Amount ->
      when {
        options?.inflow == true -> Strings.rulesFieldAmountInflow
        options?.outflow == true -> Strings.rulesFieldAmountOutflow
        else -> Strings.rulesFieldAmount
      }
    Field.Category -> Strings.rulesFieldCategory
    Field.CategoryGroup -> Strings.rulesFieldCategoryGroup
    Field.Date -> Strings.rulesFieldDate
    Field.Notes -> Strings.rulesFieldNotes
    Field.Description,
    Field.Payee -> Strings.rulesFieldPayee
    Field.PayeeName -> Strings.rulesFieldPayeeName
    Field.ImportedDescription,
    Field.ImportedPayee -> Strings.rulesFieldImportedPayee
    Field.Saved -> Strings.rulesFieldSaved
    Field.Transfer -> Strings.rulesFieldTransfer
    Field.Parent -> Strings.rulesFieldParent
    Field.Cleared -> Strings.rulesFieldCleared
    Field.Reconciled -> Strings.rulesFieldReconciled
  }

// From getRecurringDescription in packages/loot-core/src/shared/schedules.ts
internal fun RecurConfig.string(dateFormat: DateTimeFormat<LocalDate>): String {
  val endModeSuffix =
    when (endMode) {
      RecurEndMode.AfterNOccurrences -> if (endOccurrences == 1) "once" else "$endOccurrences times"
      RecurEndMode.OnDate -> "until ${endDate?.let(dateFormat::format)}"
      RecurEndMode.Never -> null
      null -> null
    }

  val weekendSolveSuffix =
    when (weekendSolveMode) {
        WeekendSolveMode.After -> "(after weekend)"
        WeekendSolveMode.Before -> "(before weekend)"
        null -> ""
      }
      .takeIf { skipWeekend == true }
      .orEmpty()

  val suffix = endModeSuffix?.let { ", $it $weekendSolveSuffix" } ?: weekendSolveSuffix

  val dt = interval ?: 1
  val desc =
    when (frequency) {
      RecurFrequency.Daily -> {
        if (dt != 1) {
          "Every $dt days"
        } else {
          "Every day"
        }
      }
      RecurFrequency.Weekly -> {
        if (dt != 1) {
          "Every $dt weeks on ${start.dayOfWeek.nice}"
        } else {
          "Every week on ${start.dayOfWeek.nice}"
        }
      }
      RecurFrequency.Monthly -> {
        monthlyRecurConfigDesc()
      }
      RecurFrequency.Yearly -> {
        val dateStr = "${start.month.nice} ${numberSuffix(start.day)}"
        if (dt != 1) "Every $dt years on $dateStr" else "Every year on $dateStr"
      }
    }

  return "$desc$suffix".trim()
}

private fun RecurConfig.monthlyRecurConfigDesc(): String {
  val patterns = patterns
  val interval = interval ?: 1
  return if (!patterns.isNullOrEmpty()) {
    // Sort the days ascending. We filter out -1 because that represents "last days" and should
    // always be last, but this sort would put them first
    val sortedPatterns =
      patterns
        .asSequence()
        .sortedWith(RecurPatternComparator)
        .filter { it.value != -1 }
        .plus(patterns.filter { it.value == -1 }) // Add on all -1 values to the end
        .toList()

    val strings = mutableListOf<String>()
    val uniqueDays = sortedPatterns.fastMap { it.type }.distinct()
    val isSameDay = uniqueDays.size == 1 && RecurType.Day !in uniqueDays
    sortedPatterns.forEach { p ->
      strings +=
        if (p.type == RecurType.Day) {
          if (p.value == -1) "last day" else numberSuffix(p.value)
        } else if (isSameDay) {
          if (p.value == -1) "last" else numberSuffix(p.value)
        } else {
          if (p.value == -1) {
            "last " + dayName(p.type)
          } else {
            numberSuffix(p.value) + " " + dayName(p.type)
          }
        }
    }

    var range = ""
    if (strings.size > 2) {
      range += strings.slice(0..<strings.size - 1).joinToString(separator = ", ")
      range += ", and "
      range += strings.last()
    } else {
      range += strings.joinToString(separator = " and ")
    }

    if (isSameDay) {
      range += " " + dayName(sortedPatterns[0].type)
    }

    if (interval != 1) {
      "Every $interval months on the $range"
    } else {
      "Every month on the $range"
    }
  } else {
    val day = numberSuffix(start.day)
    if (interval != 1) {
      "Every $interval months on the $day"
    } else {
      "Every month on the $day"
    }
  }
}

private object RecurPatternComparator : Comparator<RecurPattern> {
  private val RecurType.sortValue
    get() = if (this == RecurType.Day) 1 else 0

  override fun compare(p1: RecurPattern, p2: RecurPattern): Int {
    val typeOrder = p1.type.sortValue - p2.type.sortValue
    val valueOrder = p1.value - p2.value
    return if (typeOrder == 0) valueOrder else typeOrder
  }
}

private fun numberSuffix(number: Int): String {
  if (number in 10..19) return "${number}th"
  return when (number % 10) {
    1 -> "${number}st"
    2 -> "${number}nd"
    3 -> "${number}rd"
    else -> "${number}th"
  }
}

private fun dayName(type: RecurType): String =
  when (type) {
    RecurType.Sunday -> "Sunday"
    RecurType.Monday -> "Monday"
    RecurType.Tuesday -> "Tuesday"
    RecurType.Wednesday -> "Wednesday"
    RecurType.Thursday -> "Thursday"
    RecurType.Friday -> "Friday"
    RecurType.Saturday -> "Saturday"
    RecurType.Day -> error("Should never happen")
  }

private val DayOfWeek.nice: String
  get() =
    when (this) {
      DayOfWeek.SUNDAY -> "Sunday"
      DayOfWeek.MONDAY -> "Monday"
      DayOfWeek.TUESDAY -> "Tuesday"
      DayOfWeek.WEDNESDAY -> "Wednesday"
      DayOfWeek.THURSDAY -> "Thursday"
      DayOfWeek.FRIDAY -> "Friday"
      DayOfWeek.SATURDAY -> "Saturday"
    }

private val Month.nice: String
  get() =
    when (this) {
      Month.JANUARY -> "January"
      Month.FEBRUARY -> "February"
      Month.MARCH -> "March"
      Month.APRIL -> "April"
      Month.MAY -> "May"
      Month.JUNE -> "June"
      Month.JULY -> "July"
      Month.AUGUST -> "August"
      Month.SEPTEMBER -> "September"
      Month.OCTOBER -> "October"
      Month.NOVEMBER -> "November"
      Month.DECEMBER -> "December"
    }
