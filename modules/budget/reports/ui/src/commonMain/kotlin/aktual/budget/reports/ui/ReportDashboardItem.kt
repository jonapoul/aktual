/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.budget.reports.ui

import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.vm.ReportDashboardItem
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ReportDashboardItem(
  item: ReportDashboardItem,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  var showContextMenu by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .padding(4.dp)
      .fillMaxWidth()
      .wrapContentHeight()
      .background(theme.tableBackground, CardShape)
      .combinedClickable(
        onClick = { onAction(Action.OpenItem(item.id)) },
        onLongClick = { showContextMenu = true },
      ).padding(8.dp),
  ) {
    ReportDropDownMenu(
      item = item,
      expanded = showContextMenu,
      onDismiss = { showContextMenu = false },
      onAction = onAction,
    )

    ReportChart(
      modifier = Modifier.fillMaxWidth(),
      data = item.data,
      compact = true,
      onAction = onAction,
      theme = theme,
    )
  }
}

@Composable
private fun ReportDropDownMenu(
  item: ReportDashboardItem,
  expanded: Boolean,
  onDismiss: () -> Unit,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) = DropdownMenu(
  modifier = modifier,
  expanded = expanded,
  onDismissRequest = onDismiss,
) {
  DropdownMenuItem(
    text = { Text(Strings.reportsDashboardRename) },
    leadingIcon = { Icon(Icons.Filled.Edit, Strings.reportsDashboardRename) },
    onClick = {
      onDismiss()
      onAction(Action.Rename(item.id))
    },
  )
  DropdownMenuItem(
    text = { Text(Strings.reportsDashboardDelete) },
    leadingIcon = { Icon(Icons.Filled.Delete, Strings.reportsDashboardDelete) },
    onClick = {
      onDismiss()
      onAction(Action.Delete(item.id))
    },
  )
}
