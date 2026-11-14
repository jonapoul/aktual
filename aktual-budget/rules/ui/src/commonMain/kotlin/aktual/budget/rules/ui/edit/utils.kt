package aktual.budget.rules.ui.edit

import aktual.budget.model.RuleStage
import aktual.core.l10n.Strings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

internal val CARD_PADDING = PaddingValues(10.dp)

@Composable
internal fun RuleStage.string(): String =
  when (this) {
    Pre -> Strings.rulesStagePre
    Default -> Strings.rulesStageNone
    Post -> Strings.rulesStagePost
    Unknown -> Strings.rulesStageUnknown
  }

internal enum class Mode {
  Create,
  Edit,
}
