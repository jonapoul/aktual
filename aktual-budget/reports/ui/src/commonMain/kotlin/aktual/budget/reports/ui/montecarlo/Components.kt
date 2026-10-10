package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.montecarlo.MonteCarloSection
import aktual.core.icons.material.ArrowDropDown
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.redacted
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition.Companion.Above
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.launch

// One section of the page on its own card
@Composable
internal fun SectionCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) =
  Column(
    modifier = modifier.fillMaxWidth().background(colors.tableBackground, CardShape).padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    content = content,
  )

// A SectionCard that expands and collapses from its title
@Composable
internal fun CollapsibleSectionCard(
  title: String,
  section: MonteCarloSection,
  collapsed: ImmutableSet<MonteCarloSection>,
  onAction: MonteCarloActionHandler,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) =
  SectionCard(modifier = modifier) {
    val isExpanded = section !in collapsed
    val toggleLabel =
      if (isExpanded) Strings.monteCarloSectionCollapse else Strings.monteCarloSectionExpand
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .clip(CardShape)
          .clickable(
            onClickLabel = toggleLabel,
            role = Button,
            onClick = { onAction(MonteCarloAction.ToggleSection(section)) },
          ),
      verticalAlignment = CenterVertically,
    ) {
      Icon(
        imageVector = if (isExpanded) MaterialIcons.ArrowDropDown else MaterialIcons.ArrowRight,
        contentDescription = null,
        tint = colors.pageText,
      )
      SectionTitle(modifier = Modifier.weight(1f), text = title)
    }
    if (isExpanded) content()
  }

@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier,
    text = text,
    style = typography.titleMedium,
    fontWeight = SemiBold,
    color = colors.pageText,
  )

// GROUP_HEADING_STYLE: the small uppercase heading on stat tiles, field groups and tables
@Composable
internal fun GroupHeading(
  text: String,
  modifier: Modifier = Modifier,
  textAlign: TextAlign? = null,
) =
  Text(
    modifier = modifier,
    text = text.uppercase(),
    textAlign = textAlign,
    style = typography.labelSmall,
    fontWeight = SemiBold,
    letterSpacing = 0.5.sp,
    color = colors.pageText,
  )

@Composable
internal fun BodyText(text: String, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier,
    text = text.redacted(),
    style = typography.bodyMedium,
    color = colors.pageText,
  )

// MonteCarloHelpTooltip: a small info icon that shows its text when tapped
@Composable
internal fun HelpTooltip(text: String, modifier: Modifier = Modifier) {
  val state = rememberTooltipState(isPersistent = true)
  val scope = rememberCoroutineScope()
  TooltipBox(
    modifier = modifier,
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(Above),
    state = state,
    tooltip = {
      PlainTooltip(
        shape = CardShape,
        contentColor = colors.tooltipText,
        containerColor = colors.tooltipBackground,
        content = { Text(text) },
      )
    },
  ) {
    Icon(
      modifier =
        Modifier.size(HELP_ICON_SIZE).clickable {
          if (state.isVisible) state.dismiss() else scope.launch { state.show() }
        },
      imageVector = MaterialIcons.Info,
      contentDescription = text,
      tint = colors.pageTextSubdued,
    )
  }
}

private val HELP_ICON_SIZE = 16.dp
