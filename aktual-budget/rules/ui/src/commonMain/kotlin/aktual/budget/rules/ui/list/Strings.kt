package aktual.budget.rules.ui.list

import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleAction.AppendNotes
import aktual.budget.model.RuleAction.DeleteTransaction
import aktual.budget.model.RuleAction.LinkSchedule
import aktual.budget.model.RuleAction.Op
import aktual.budget.model.RuleAction.PrependNotes
import aktual.budget.model.RuleAction.Set as SetAction
import aktual.budget.model.RuleAction.SetSplitAmount
import aktual.budget.model.RuleStage
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.ui.AktualTypography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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
internal fun RuleStage?.string(): String =
  when (this) {
    RuleStage.Pre -> Strings.rulesStagePre
    null -> Strings.rulesStageNone
    RuleStage.Post -> Strings.rulesStagePre
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
): AnnotatedString =
  remember(prefix, condition, styles) {
    buildAnnotatedString {
      withStyle(styles.default) { append(prefix) }
      withStyle(styles.highlighted) { append(condition.field.string(condition.options)) }
      withStyle(styles.default) {
        append(" ")
        append(condition.operator.string)
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

@Composable
internal fun rememberActionText(action: RuleAction, styles: RuleSpanStyles): AnnotatedString =
  remember(action, styles) {
    buildAnnotatedString {
      when (action) {
        is AppendNotes -> append(action, styles)
        is DeleteTransaction -> append(action, styles)
        is LinkSchedule -> append(action, styles)
        is PrependNotes -> append(action, styles)
        is SetAction -> append(action, styles)
        is SetSplitAmount -> append(action, styles)
      }
    }
  }

private fun AnnotatedString.Builder.append(action: AppendNotes, styles: RuleSpanStyles) {
  withStyle(styles.default) {
    append(action.op.string)
    append(" ")
  }
  withStyle(styles.highlighted) { append(action.value) }
}

private fun AnnotatedString.Builder.append(action: DeleteTransaction, styles: RuleSpanStyles) {
  withStyle(styles.default) { append(action.op.string) }
}

private fun AnnotatedString.Builder.append(action: LinkSchedule, styles: RuleSpanStyles) {
  withStyle(styles.default) {
    append(action.op.string)
    append(" ")
  }
  withStyle(styles.highlighted) { append(action.value.toString()) }
}

private fun AnnotatedString.Builder.append(action: PrependNotes, styles: RuleSpanStyles) {
  withStyle(styles.default) {
    append(action.op.string)
    append(" ")
  }
  withStyle(styles.highlighted) { append(action.value) }
}

private fun AnnotatedString.Builder.append(action: SetAction, styles: RuleSpanStyles) {
  withStyle(styles.default) {
    append(action.op.string)
    append(" ")
  }
  withStyle(styles.highlighted) { append(action.field.value) }
  withStyle(styles.default) { append(" to ") }
  withStyle(styles.highlighted) { append(action.value) }
}

private fun AnnotatedString.Builder.append(action: SetSplitAmount, styles: RuleSpanStyles) {
  withStyle(styles.default) {
    append(action.op.string)
    append(" ")
  }
  withStyle(styles.highlighted) { append(action.value?.toString()) }
}

private val Op.string: String
  get() =
    when (this) {
      Op.Set -> "set"
      Op.SetSplitAmount -> "allocate"
      Op.LinkSchedule -> "link schedule"
      Op.PrependNotes -> "prepend to notes"
      Op.AppendNotes -> "append to notes"
      Op.DeleteTransaction -> "delete transaction"
    }

@Stable
private val Operator.string: String
  get() =
    when (this) {
      Operator.Contains -> "contains"
      Operator.DoesNotContain -> "does not contain"
      Operator.GreaterThan -> "is greater than"
      Operator.GreaterThanOrEquals -> "is greater than or equals"
      Operator.HasTags -> "has tags"
      Operator.Is -> "is"
      Operator.IsApprox -> "is approx"
      Operator.IsBetween -> "is between"
      Operator.IsNot -> "is not"
      Operator.LessThan -> "is less than"
      Operator.LessThanOrEquals -> "is less than or equals"
      Operator.Matches -> "matches"
      Operator.NotOneOf -> "not one of"
      Operator.OffBudget -> "off budget"
      Operator.OnBudget -> "on budget"
      Operator.OneOf -> "one of"
    }

@Stable
private fun Field.string(options: Condition.Options?): String =
  when (this) {
    Field.Account -> "account"
    Field.Amount ->
      when {
        options?.inflow == true -> "amount (inflow)"
        options?.outflow == true -> "amount (outflow)"
        else -> "amount"
      }
    Field.Category -> "category"
    Field.CategoryGroup -> "category group"
    Field.Date -> "date"
    Field.Description -> "description"
    Field.Notes -> "notes"
    Field.Payee -> "payee"
    Field.PayeeName -> "payee (name)"
    Field.ImportedPayee -> "imported payee"
    Field.Saved -> "saved"
    Field.Transfer -> "transfer"
    Field.Parent -> "parent"
    Field.Cleared -> "cleared"
    Field.Reconciled -> "reconciled"
  }
