package actual.budget.reports.ui

import actual.budget.model.Amount
import actual.budget.model.ReportDate
import actual.budget.reports.ui.ReportDashboardItems.ITEM_1
import actual.budget.reports.ui.ReportDashboardItems.ITEM_2
import actual.budget.reports.ui.ReportDashboardItems.ITEM_3
import actual.budget.reports.ui.charts.ReportChart
import actual.budget.reports.vm.ReportDashboardItem
import actual.budget.reports.vm.ReportRange
import actual.budget.reports.vm.ReportValues
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.formattedString
import actual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ReportDashboardItem(
  item: ReportDashboardItem,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  var showContextMenu by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .padding(5.dp)
      .fillMaxWidth()
      .wrapContentHeight()
      .background(theme.tableBackground, CardShape)
      .combinedClickable(
        onClick = { onAction(Action.OpenItem(item.id)) },
        onLongClick = { showContextMenu = true },
      ).padding(10.dp),
  ) {
    Header(
      modifier = Modifier.fillMaxWidth(),
      name = item.name,
      range = item.range,
      values = item.values,
      theme = theme,
    )

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

@Composable
private fun Header(
  name: String,
  range: ReportRange,
  values: ReportValues,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Row(
  modifier = modifier,
) {
  Column(
    modifier = Modifier.weight(1f),
    horizontalAlignment = Alignment.Start,
  ) {
    Text(
      text = name,
      textAlign = TextAlign.Start,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontWeight = FontWeight.W500,
      fontSize = 15.sp,
      color = theme.pageText,
    )
    Text(
      text = range.string(),
      textAlign = TextAlign.Start,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 13.sp,
      style = MaterialTheme.typography.bodySmall,
      color = theme.pageTextSubdued,
    )
  }

  if (values !is ReportValues.Shown) return
  Column(
    modifier = Modifier.wrapContentWidth(),
    horizontalAlignment = Alignment.End,
  ) {
    Text(
      text = values.amount.formattedString(),
      textAlign = TextAlign.End,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontWeight = FontWeight.W500,
      fontSize = 15.sp,
      color = theme.pageText,
    )
    val prefix = if (values.change >= Amount.Zero) '+' else '-'
    Text(
      text = prefix + values.change.formattedString(),
      textAlign = TextAlign.End,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 13.sp,
      style = MaterialTheme.typography.bodySmall,
      color = when {
        values.change >= Amount.Zero -> theme.noticeTextLight
        else -> theme.errorText
      },
    )
  }
}

@Composable
@ReadOnlyComposable
private fun ReportRange.string(): String = when (this) {
  is ReportRange.Dynamic -> type.value
  is ReportRange.Static -> Strings.reportsDashboardRange(start.string(), end.string())
}

@Composable
@ReadOnlyComposable
private fun ReportDate.string(): String = when (this) {
  is ReportDate.Month -> yearMonth.toString()
  is ReportDate.Date -> date.toString()
}

@Preview
@Composable
private fun PreviewItem1() = PreviewColumn {
  ReportDashboardItem(
    item = ITEM_1,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewItem2() = PreviewColumn {
  ReportDashboardItem(
    item = ITEM_2,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewItem3() = PreviewColumn {
  ReportDashboardItem(
    item = ITEM_3,
    onAction = {},
  )
}
