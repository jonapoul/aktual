package aktual.budget.rules.ui.list

import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleAction.AppendNotes
import aktual.budget.model.RuleAction.DeleteTransaction
import aktual.budget.model.RuleAction.LinkSchedule
import aktual.budget.model.RuleAction.PrependNotes
import aktual.budget.model.RuleAction.Set as SetAction
import aktual.budget.model.RuleAction.SetSplitAmount
import aktual.budget.model.RuleStage
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.ui.AktualTypography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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

@Composable
internal fun rememberConditionText(
  prefix: String,
  condition: Condition,
  styles: RuleSpanStyles,
): AnnotatedString {
  val fieldText = condition.field.string(condition.options)
  val operatorText = condition.operator.displayString()
  return remember(prefix, condition, styles, fieldText, operatorText) {
    buildAnnotatedString {
      withStyle(styles.default) { append(prefix) }
      withStyle(styles.highlighted) { append(fieldText) }
      withStyle(styles.default) {
        append(" ")
        append(operatorText)
        append(" ")
      }
      when (val value = condition.value) {
        is JsonPrimitive -> {
          withStyle(styles.highlighted) { append(value.content) }
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

        JsonNull,
        is JsonObject -> {
          error("Should never see this in a condition's value: $value")
        }
      }
    }
  }
}

@Composable
internal fun rememberActionText(action: RuleAction, styles: RuleSpanStyles): AnnotatedString {
  val opText = action.opString()
  val fieldText = (action as? SetAction)?.field?.string(options = null)
  val setToText = if (action is SetAction) Strings.rulesActionSetTo else null
  return remember(action, styles, opText, fieldText, setToText) {
    buildAnnotatedString {
      when (action) {
        is AppendNotes -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) { append(action.value) }
        }
        is DeleteTransaction -> {
          withStyle(styles.default) { append(opText) }
        }
        is LinkSchedule -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) { append(action.value.toString()) }
        }
        is PrependNotes -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) { append(action.value) }
        }
        is SetAction -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) { append(fieldText) }
          withStyle(styles.default) { append(setToText) }
          withStyle(styles.highlighted) { append(action.value) }
        }
        is SetSplitAmount -> {
          withStyle(styles.default) {
            append(opText)
            append(" ")
          }
          withStyle(styles.highlighted) { append(action.value?.toString()) }
        }
      }
    }
  }
}

@Composable
private fun RuleAction.opString(): String =
  when (this) {
    is AppendNotes -> Strings.rulesOpAppendNotes
    is DeleteTransaction -> Strings.rulesOpDeleteTransaction
    is LinkSchedule -> Strings.rulesOpLinkSchedule
    is PrependNotes -> Strings.rulesOpPrependNotes
    is SetAction -> Strings.rulesOpSet
    is SetSplitAmount -> Strings.rulesOpSetSplitAmount
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
private fun Field.string(options: Condition.Options?): String =
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
    Field.Description -> Strings.rulesFieldDescription
    Field.Notes -> Strings.rulesFieldNotes
    Field.Payee -> Strings.rulesFieldPayee
    Field.PayeeName -> Strings.rulesFieldPayeeName
    Field.ImportedDescription -> Strings.rulesFieldImportedDescription
    Field.ImportedPayee -> Strings.rulesFieldImportedPayee
    Field.Saved -> Strings.rulesFieldSaved
    Field.Transfer -> Strings.rulesFieldTransfer
    Field.Parent -> Strings.rulesFieldParent
    Field.Cleared -> Strings.rulesFieldCleared
    Field.Reconciled -> Strings.rulesFieldReconciled
  }
