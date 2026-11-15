@file:Suppress("MagicNumber")

package aktual.budget.rules.ui.list

import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.Field.Amount
import aktual.budget.model.NumberFormatConfig
import aktual.budget.model.RecurConfig
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleAction.Op.Set
import aktual.budget.model.RuleStage
import aktual.budget.model.ScheduleId
import aktual.budget.model.isIdField
import aktual.budget.rules.domain.NameFetcher
import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.ui.displayString
import aktual.budget.rules.ui.list.Name.Loaded
import aktual.budget.rules.ui.string
import aktual.budget.rules.vm.label
import aktual.budget.schedules.components.description
import aktual.core.l10n.Strings
import aktual.core.ui.LocalCurrencyConfig
import aktual.core.ui.LocalDateFormatter
import aktual.core.ui.LocalNumberFormatConfig
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.ShimmerBlock
import aktual.core.ui.highlighted
import aktual.core.ui.redacted
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.stringResource

@Composable internal fun RuleStage.string(): String = stringResource(label())

private fun JsonArray.toList(): List<String> = map { it.jsonPrimitive.content }

private fun Condition.recurConfig(): RecurConfig? =
  (value as? JsonObject)
    ?.takeIf { field == Date }
    ?.let { Json.decodeFromJsonElement(RecurConfig.serializer(), it) }

@Composable
internal fun rememberConditionText(
  prefix: String,
  condition: Condition,
  styles: RuleSpanStyles,
  query: String = "",
): AnnotatedString {
  val fieldText = condition.field.string(condition.options)
  val opText = condition.operator.displayString()

  val dateFormat = LocalDateFormatter.current
  val numberFormat = LocalNumberFormatConfig.current
  val currency = LocalCurrencyConfig.current
  val privacy = LocalPrivacyEnabled.current

  val nameFetcher = LocalNameFetcher.current
  val fieldNames = rememberName(nameFetcher, condition) { fieldNamesFlow(condition, nameFetcher) }
  val recurConfig = remember(condition) { condition.recurConfig() }
  val recurText = recurConfig?.description(dateFormat)

  return remember(
      condition,
      styles,
      opText,
      fieldText,
      fieldNames,
      recurText,
      numberFormat,
      currency,
      privacy,
      query,
    ) {
      buildAnnotatedString {
          withStyle(styles.default) { append(prefix) }
          withStyle(styles.highlighted) { append(fieldText) }
          withStyle(styles.default) {
            append(" ")
            append(opText)
            append(" ")
          }
          val value = (fieldNames as? Loaded)?.value ?: condition.value
          when {
            fieldNames is Loading -> appendNamePlaceholder(styles)

            value is JsonPrimitive -> {
              withStyle(styles.highlighted) {
                if (condition.field == Amount) {
                  append(
                    Amount(value.int)
                      .toString(
                        numberFormatConfig = numberFormat,
                        currencyConfig = currency,
                        includeSign = true,
                        isPrivacyEnabled = privacy,
                      ),
                  )
                } else {
                  append(value.content)
                }
              }
            }

            value is JsonArray -> {
              withStyle(styles.default) { append("[") }
              value.forEachIndexed { index, element ->
                withStyle(styles.highlighted) { append(element.jsonPrimitive.content) }
                if (index != value.lastIndex) {
                  withStyle(styles.default) { append(", ") }
                }
              }
              withStyle(styles.default) { append("]") }
            }

            value is JsonObject -> {
              if (condition.field != Date || recurText == null) {
                error("Should only see a JSON object in a condition value for a date: $condition")
              }
              withStyle(styles.highlighted) { append(recurText) }
            }
          }
        }
        .highlighted(query)
    }
    .redacted()
}

private fun fieldNamesFlow(condition: Condition, nameFetcher: NameFetcher): Flow<JsonElement?>? =
  when (condition.field) {
    Acct,
    Account,
    Category,
    CategoryGroup,
    Description,
    Payee ->
      when (val value = condition.value) {
        is JsonArray -> nameFetcher(condition.field, value.toList())
        is JsonPrimitive ->
          nameFetcher(condition.field, value.content).map { it?.let(::JsonPrimitive) }

        else -> null
      }

    Amount,
    Cleared,
    Date,
    ImportedDescription,
    ImportedPayee,
    Notes,
    Parent,
    PayeeName,
    Reconciled,
    Saved,
    Transfer,
    Unknown -> null
  }

@Composable
internal fun rememberActionText(
  action: RuleAction,
  styles: RuleSpanStyles,
  query: String = "",
): AnnotatedString {
  val opText = action.opString()
  val fieldText = action.field?.string(options = null)
  val setToText = if (action.op == Set) Strings.rulesActionSetTo else null

  val numberFormat = LocalNumberFormatConfig.current
  val currency = LocalCurrencyConfig.current
  val privacy = LocalPrivacyEnabled.current

  val nameFetcher = LocalNameFetcher.current
  val name = rememberName(nameFetcher, action) { actionNameFlow(action, nameFetcher) }

  return remember(
      action,
      styles,
      opText,
      fieldText,
      setToText,
      name,
      numberFormat,
      currency,
      privacy,
      query,
    ) {
      buildAnnotatedString {
          val content = action.value?.content.orEmpty()
          val fieldName = (name as? Loaded)?.value
          when (action.op) {
            AppendNotes -> {
              withStyle(styles.default) {
                append(opText)
                append(" ")
              }
              withStyle(styles.highlighted) { append(content) }
            }
            DeleteTransaction,
            Unknown -> withStyle(styles.default) { append(opText) }
            LinkSchedule -> {
              withStyle(styles.default) {
                append(opText)
                append(" ")
              }
              if (name is Loading) {
                appendNamePlaceholder(styles)
              } else {
                withStyle(styles.highlighted) { append(fieldName ?: content) }
              }
            }
            PrependNotes -> {
              withStyle(styles.default) {
                append(opText)
                append(" ")
              }
              withStyle(styles.highlighted) { append(content) }
            }
            Set -> {
              withStyle(styles.default) {
                append(opText)
                append(" ")
              }
              withStyle(styles.highlighted) { append(fieldText) }
              withStyle(styles.default) { append(setToText) }
              if (name is Loading) {
                appendNamePlaceholder(styles)
              } else {
                withStyle(styles.highlighted) {
                  if (action.field == Amount) {
                    append(formatAmount(action, numberFormat, currency, privacy))
                  } else {
                    append(fieldName ?: content)
                  }
                }
              }
            }
            SetSplitAmount -> {
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
        .highlighted(query)
    }
    .redacted()
}

@Suppress("ElseCaseInsteadOfExhaustiveWhen")
private fun actionNameFlow(action: RuleAction, nameFetcher: NameFetcher): Flow<String?>? {
  val value = action.value
  return when (action.op) {
    Set -> {
      val field = action.field
      if (field.isIdField() && value != null) {
        nameFetcher(field, value.content)
      } else {
        null
      }
    }

    LinkSchedule -> value?.let { nameFetcher(ScheduleId(it.content)) }

    else -> null
  }
}

private fun formatAmount(
  action: RuleAction,
  numberFormat: NumberFormatConfig,
  currency: CurrencyConfig,
  privacy: Boolean,
): String =
  Amount(action.value?.content?.toInt() ?: 0)
    .toString(
      numberFormatConfig = numberFormat,
      currencyConfig = currency,
      includeSign = true,
      isPrivacyEnabled = privacy,
    )

@Composable private fun RuleAction.opString(): String = stringResource(op.label())

private sealed interface Name<out T> {
  data object Loading : Name<Nothing>

  @JvmInline value class Loaded<T>(val value: T?) : Name<T>
}

// Loading until the first emission, or loaded with null straight away when there's nothing to fetch
@Composable
private fun <T : Any> rememberName(vararg keys: Any?, flow: () -> Flow<T?>?): Name<T> {
  val names = remember(*keys) { flow()?.map { Loaded(it) } }
  val initial: Name<T> = if (names == null) Loaded(null) else Loading
  val name by (names ?: emptyFlow()).collectAsStateWithLifecycle(initialValue = initial)
  return name
}

private fun AnnotatedString.Builder.appendNamePlaceholder(styles: RuleSpanStyles) =
  withStyle(styles.highlighted) { appendInlineContent(NAME_PLACEHOLDER_ID, "...") }

// Pass to any Text showing rememberConditionText or rememberActionText
@Composable
internal fun rememberNameInlineContent(): ImmutableMap<String, InlineTextContent> {
  val shimmer = rememberShimmer(Window)
  return remember(shimmer) {
    persistentMapOf(
      NAME_PLACEHOLDER_ID to
        InlineTextContent(InlinePillPlaceholder) {
          ShimmerBlock(modifier = Modifier.fillMaxSize().shimmer(shimmer), shape = InlinePillShape)
        },
    )
  }
}

private val InlinePillPlaceholder = Placeholder(5.em, 1.em, Center)
private val InlinePillShape = RoundedCornerShape(2.dp)
private const val NAME_PLACEHOLDER_ID = "name-placeholder"
