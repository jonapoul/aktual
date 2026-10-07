package aktual.budget.reports.ui.dashboard

import aktual.budget.model.DashboardPageId
import aktual.budget.reports.ui.Action.SelectPage
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.core.icons.material.ArrowDropDown
import aktual.core.icons.material.Check
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun DashboardSelector(
  pages: ImmutableList<DashboardPage>,
  selected: DashboardPage?,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  if (selected == null) {
    Text(modifier = modifier, text = Strings.reportsDashboardTitle, color = colors.pageText)
    return
  }

  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    Row(
      modifier =
        Modifier.background(colors.pillBackgroundLight, CardShape)
          .clickable { expanded = true }
          .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = CenterVertically,
    ) {
      Text(
        text = selected.displayName(),
        color = colors.pageText,
        maxLines = 1,
        overflow = Ellipsis,
      )
      Icon(
        imageVector = MaterialIcons.ArrowDropDown,
        contentDescription = Strings.reportsDashboardChoose,
        tint = colors.pageText,
      )
    }

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      DashboardSelectorItems(
        pages = pages,
        selected = selected,
        onAction = { action ->
          expanded = false
          onAction(action)
        },
      )
    }
  }
}

@Composable
private fun DashboardSelectorItems(
  pages: ImmutableList<DashboardPage>,
  selected: DashboardPage,
  onAction: ActionListener,
) = pages.fastForEach { page ->
  AktualDropdownMenuItem(
    text = page.displayName(),
    trailingIcon = if (page.id == selected.id) MaterialIcons.Check else null,
    onClick = { onAction(SelectPage(page.id)) },
  )
}

@Composable
internal fun DashboardPage.displayName(): String = name.ifBlank { Strings.reportsDashboardUntitled }

@Preview
@Composable
private fun PreviewDashboardSelector(
  @PreviewParameter(DashboardSelectorItemsProvider::class)
  params: ColoredParams<ImmutableList<DashboardPage>>,
) =
  PreviewWithColoredParams(params) {
    DashboardSelector(
      pages = this,
      selected = first(),
      onAction = {},
    )
  }

@Preview
@Composable
private fun PreviewDashboardSelectorItems(
  @PreviewParameter(DashboardSelectorItemsProvider::class)
  params: ColoredParams<ImmutableList<DashboardPage>>,
) =
  PreviewWithColoredParams(params) {
    Column(Modifier.width(IntrinsicSize.Max).background(colors.menuBackground)) {
      DashboardSelectorItems(pages = params.data, selected = params.data.first(), onAction = {})
    }
  }

internal val PREVIEW_PAGES =
  persistentListOf(
    DashboardPage(DashboardPageId("a"), "Main"),
    DashboardPage(DashboardPageId("b"), ""),
  )

private class DashboardSelectorItemsProvider :
  ColoredParameterProvider<ImmutableList<DashboardPage>>(
    PREVIEW_PAGES,
    persistentListOf(PREVIEW_PAGES[0]),
  )
