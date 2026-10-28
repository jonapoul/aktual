package aktual.budget.rules.ui.list

import aktual.budget.model.RuleStage
import aktual.budget.rules.vm.Rule
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.StuckHeaders
import aktual.core.ui.stuckHeader
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList

// Rules arrive sorted in execution order, so grouping keeps stages in run order too
internal fun LazyListScope.ruleSections(
  rules: ImmutableList<Rule>,
  stuck: StuckHeaders,
  itemContent: @Composable LazyItemScope.(Rule) -> Unit,
) {
  rules.groupBy(Rule::stage).entries.forEachIndexed { index, (stage, stageRules) ->
    // Spaces sections apart without padding the header, which would carry the gap when stuck
    if (index > 0) {
      item(key = "stage-gap-${stage.name}", contentType = "stage-gap") {
        VerticalSpacer(SectionGap, modifier = Modifier.animateItem())
      }
    }

    stuckHeader(key = "stage-${stage.name}", stuck = stuck) { modifier ->
      RuleStageHeader(stage = stage, count = stageRules.size, modifier = modifier.animateItem())
    }

    items(stageRules, key = { it.id.value }, contentType = { "rule" }, itemContent = itemContent)
  }
}

@Composable
internal fun RuleStageHeader(stage: RuleStage, count: Int, modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.pageBackground)
        .padding(horizontal = 8.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
    verticalAlignment = Alignment.Bottom,
  ) {
    Text(
      text = stage.string(),
      color = colors.pageText,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
    )

    Text(
      modifier = Modifier.weight(1f),
      text = stage.hint().orEmpty(),
      color = colors.pageTextLight,
      fontSize = 12.sp,
    )

    Text(
      text = Plurals.rulesStageCount(count, count),
      color = colors.pageTextLight,
      fontSize = 12.sp,
    )
  }
}

// On top of the list's own item spacing either side
private val SectionGap = 6.dp

@Composable
private fun RuleStage.hint(): String? =
  when (this) {
    Pre -> Strings.rulesStageHintPre
    Post -> Strings.rulesStageHintPost
    Default,
    Unknown -> null
  }

@Preview
@Composable
private fun PreviewRuleStageHeader(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    RuleStageHeader(stage = Pre, count = 3)
  }
