package aktual.budget.rules.ui.edit

import aktual.budget.model.ConditionOp
import aktual.budget.model.RuleStage
import aktual.core.l10n.Strings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

internal val CARD_PADDING = PaddingValues(10.dp)

@Composable
internal fun RuleStage.string(): String =
  when (this) {
    RuleStage.Pre -> Strings.rulesStagePre
    RuleStage.Default -> Strings.rulesStageNone
    RuleStage.Post -> Strings.rulesStagePost
  }

internal enum class Mode {
  Create,
  Edit,
}

@Composable
internal fun ConditionOp.string(): String =
  when (this) {
    ConditionOp.And -> Strings.editRuleAnd
    ConditionOp.Or -> Strings.editRuleOr
  }

internal val BUTTON_PADDING = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
