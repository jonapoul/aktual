package aktual.budget.reports.ui.montecarlo

import aktual.budget.reports.vm.montecarlo.MonteCarloSection
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.icons.CheveronRight
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.letterSpacings
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardColumn
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.OnAction
import aktual.core.ui.ProvideSurfaceColor
import aktual.core.ui.padding
import aktual.core.ui.redacted
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableSet

// One section of the page on its own card
@Composable
internal fun SectionCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) =
  CardColumn(
    modifier = modifier.fillMaxWidth(),
    contentPadding = Dimens.Huge.padding,
    verticalArrangement = Arrangement.spacedBy(10.dp),
    content = { ProvideSurfaceColor(colors.background) { content() } },
  )

// A SectionCard that expands and collapses from its title
@Composable
internal fun CollapsibleSectionCard(
  title: String,
  section: MonteCarloSection,
  collapsed: ImmutableSet<MonteCarloSection>,
  onAction: OnAction<MonteCarloAction>,
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
        imageVector = if (isExpanded) AktualIcons.CheveronDown else AktualIcons.CheveronRight,
        contentDescription = null,
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
    letterSpacing = letterSpacings.wide,
  )

@Composable
internal fun BodyText(text: String, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier,
    text = text.redacted(),
    style = typography.bodyMedium,
  )
