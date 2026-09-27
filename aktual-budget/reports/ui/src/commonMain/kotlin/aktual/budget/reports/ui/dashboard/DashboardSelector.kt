package aktual.budget.reports.ui.dashboard

import aktual.budget.reports.ui.Action
import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.core.icons.material.ArrowDropDown
import aktual.core.icons.material.Check
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun DashboardSelector(
  pages: ImmutableList<DashboardPage>,
  selected: DashboardPage?,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  if (pages.size <= 1 || selected == null) {
    Text(modifier = modifier, text = Strings.reportsDashboardTitle, color = colors.pageText)
    return
  }

  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    Row(
      modifier = Modifier.clickable { expanded = true }.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = CenterVertically,
    ) {
      Text(
        text = selected.displayName(),
        color = colors.pageText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Icon(
        imageVector = MaterialIcons.ArrowDropDown,
        contentDescription = Strings.reportsDashboardChoose,
        tint = colors.pageText,
      )
    }

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      pages.fastForEach { page ->
        AktualDropdownMenuItem(
          text = page.displayName(),
          trailingIcon = if (page.id == selected.id) MaterialIcons.Check else null,
          onClick = {
            expanded = false
            onAction(Action.SelectPage(page.id))
          },
        )
      }
    }
  }
}

@Composable
private fun DashboardPage.displayName(): String = name.ifBlank { Strings.reportsDashboardUntitled }
