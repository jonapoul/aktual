package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.CustomGroup
import aktual.budget.reports.vm.CustomGroupTotals
import aktual.budget.reports.vm.CustomTotals
import aktual.budget.reports.vm.metric
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.formattedText
import aktual.core.ui.horizontalScrollWithBar
import aktual.core.ui.scrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.roundToLong
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// packages/desktop-client/src/components/reports/graphs/tableGraph/ReportTable.tsx: a row per group
// under a header, with the totals at the bottom. The name column stays put while the amounts scroll
// sideways together
@Composable
internal fun CustomTable(data: CustomData, compact: Boolean, modifier: Modifier = Modifier) {
  val headers = customTableHeaders(data)
  val names = buildMap {
    data.groups.forEach { put(it.group, it.group.label()) }
    data.categoryGroups.forEach { group ->
      put(group.group, group.group.label())
      group.categories.forEach { put(it.group, it.group.label()) }
    }
  }
  val intervalNames = intervalLabels(data)
  val totalsName = Strings.reportsCustomTableTotals
  val rows =
    remember(data, names, intervalNames) {
      customTableRows(data, { names[it].orEmpty() }, intervalNames)
    }
  val totals = remember(data, totalsName) { customTableTotals(data, totalsName) }

  BoxWithConstraints(modifier = modifier) {
    val nameWidth = if (compact) COMPACT_NAME_WIDTH else NAME_WIDTH
    val minAmountWidth = if (compact) COMPACT_AMOUNT_WIDTH else AMOUNT_WIDTH
    val amountWidth = max(minAmountWidth, (maxWidth - nameWidth) / headers.size)
    val widths = TableWidths(nameWidth, amountWidth)
    val textStyle = if (compact) typography.labelSmall else typography.bodySmall
    val rowHeight = if (compact) COMPACT_ROW_HEIGHT else ROW_HEIGHT
    val scroll = rememberScrollState()

    Column {
      TableRow(
        name = headers.first(),
        widths = widths,
        height = rowHeight,
        background = colors.tableHeaderBackground,
        scroll = scroll,
      ) {
        headers.drop(1).fastForEach { header ->
          TableCell(
            text = AnnotatedString(header),
            width = widths.amount,
            style = textStyle,
            color = colors.tableHeaderText,
            isBold = true,
          )
        }
      }
      HorizontalDivider(color = colors.tableBorder)

      val listState = rememberLazyListState()
      LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth().scrollbar(listState),
        state = listState,
        userScrollEnabled = !compact,
      ) {
        items(rows) { row ->
          when (row) {
            is Amounts -> AmountsRow(row, widths, rowHeight, textStyle, scroll)
            Gap -> Spacer(modifier = Modifier.height(GAP_HEIGHT))
          }
        }
      }

      HorizontalDivider(color = colors.tableBorder)
      AmountsRow(
        row = totals,
        widths = widths,
        height = rowHeight,
        textStyle = textStyle,
        scroll = scroll,
        isTotals = true,
        showScrollbar = !compact,
      )
    }
  }
}

@Immutable private data class TableWidths(val name: Dp, val amount: Dp)

@Composable
private fun AmountsRow(
  row: CustomTableRow.Amounts,
  widths: TableWidths,
  height: Dp,
  textStyle: TextStyle,
  scroll: ScrollState,
  isTotals: Boolean = false,
  showScrollbar: Boolean = false,
) {
  val isHeader = isTotals || row.isGroupHeader
  val background = if (isHeader) colors.tableRowHeaderBackground else colors.tableBackground
  val textColor = if (isHeader) colors.tableRowHeaderText else colors.tableText
  val boldFrom = row.amounts.size - BOLD_COLUMNS
  TableRow(
    name = row.name,
    widths = widths,
    height = height,
    background = background,
    textColor = textColor,
    textStyle = textStyle,
    isBold = isHeader,
    scroll = scroll,
    showScrollbar = showScrollbar,
  ) {
    row.amounts.fastForEachIndexed { index, amount ->
      TableCell(
        text = amount.formattedText(),
        width = widths.amount,
        style = textStyle,
        color = if (isTotals) colors.amountColor(amount) else textColor,
        isBold = isHeader || index >= boldFrom,
      )
    }
  }
}

@Composable
private fun TableRow(
  name: String,
  widths: TableWidths,
  height: Dp,
  background: Color,
  scroll: ScrollState,
  showScrollbar: Boolean = false,
  textColor: Color = colors.tableHeaderText,
  textStyle: TextStyle = typography.bodySmall,
  isBold: Boolean = true,
  amounts: @Composable () -> Unit,
) =
  Row(
    modifier = Modifier.fillMaxWidth().heightIn(min = height).background(background),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.width(widths.name).padding(horizontal = CELL_PADDING),
      text = name,
      style = textStyle,
      color = textColor,
      fontWeight = if (isBold) FontWeight.SemiBold else null,
      maxLines = 1,
      overflow = Ellipsis,
    )
    Row(
      modifier =
        if (showScrollbar) {
          Modifier.horizontalScrollWithBar(scroll)
        } else {
          Modifier.horizontalScroll(scroll)
        },
    ) {
      amounts()
    }
  }

@Composable
private fun TableCell(
  text: AnnotatedString,
  width: Dp,
  style: TextStyle,
  color: Color,
  isBold: Boolean,
) =
  Text(
    modifier = Modifier.width(width).padding(horizontal = CELL_PADDING),
    text = text,
    style = style,
    color = color,
    fontWeight = if (isBold) FontWeight.SemiBold else null,
    textAlign = End,
    maxLines = 1,
    overflow = Ellipsis,
  )

// ReportTableRow.tsx getAmountColor()
private fun Colors.amountColor(amount: Amount): Color =
  when {
    amount == Zero -> reportsNumberNeutral
    amount > Amount.Zero -> reportsNumberPositive
    else -> reportsNumberNegative
  }

@Immutable
internal sealed interface CustomTableRow {
  data class Amounts(
    val name: String,
    val amounts: ImmutableList<Amount>,
    val isGroupHeader: Boolean = false,
  ) : CustomTableRow

  // The space upstream leaves after each category group's categories
  data object Gap : CustomTableRow
}

// ReportTableHeader.tsx: the group by column, then a column per interval in time mode, or deposits
// and payments when the report shows both
@Composable
internal fun customTableHeaders(data: CustomData): ImmutableList<String> {
  val config = data.config
  val first =
    if (config.groupBy == GroupBy.Interval) config.interval.unitName() else config.groupBy.name()
  val middle =
    when {
      config.mode == Time -> intervalLabels(data)
      config.balanceType.metric == Total ->
        listOf(Strings.reportsCustomTableDeposits, Strings.reportsCustomTablePayments)
      else -> emptyList()
    }
  return (listOf(first) +
      middle +
      Strings.reportsCustomTableTotals +
      Strings.reportsCustomTableAverage)
    .toImmutableList()
}

// ReportTableList.tsx: grouping by category shows each category group, holding its categories.
// Grouping by interval shows a row per interval
internal fun customTableRows(
  data: CustomData,
  name: (CustomGroup) -> String,
  intervalNames: List<String>,
): ImmutableList<CustomTableRow> =
  when (data.config.groupBy) {
    Category ->
      data.categoryGroups.flatMap { group ->
        listOf(data.amountsRow(group, name, isGroupHeader = true)) +
          group.categories.map { data.amountsRow(it, name) } +
          CustomTableRow.Gap
      }

    GroupBy.Interval ->
      data.intervals.mapIndexed { index, interval ->
        CustomTableRow.Amounts(
          name = intervalNames[index],
          amounts = data.amounts(interval.totals, listOf(interval.totals)),
        )
      }

    Group,
    CategoryGroup,
    Payee,
    Account,
    Unknown -> data.groups.map { data.amountsRow(it, name) }
  }.toImmutableList()

// ReportTableTotals.tsx
internal fun customTableTotals(data: CustomData, name: String) =
  CustomTableRow.Amounts(name, data.amounts(data.totals, data.intervals.map { it.totals }))

private fun CustomData.amountsRow(
  group: CustomGroupTotals,
  name: (CustomGroup) -> String,
  isGroupHeader: Boolean = false,
) =
  CustomTableRow.Amounts(
    name = name(group.group),
    amounts = amounts(group.totals, group.intervals.map { it.totals }),
    isGroupHeader = isGroupHeader,
  )

// ReportTableRow.tsx: the same columns as the header. The average is over every interval in the
// report's dates, even ones trimmed off
private fun CustomData.amounts(
  totals: CustomTotals,
  intervals: List<CustomTotals>,
): ImmutableList<Amount> {
  val metric = config.balanceType.metric
  val total = totals[metric]
  val middle =
    when {
      config.mode == Time -> intervals.map { it[metric] }
      metric == Total -> listOf(totals.assets, totals.debts)
      else -> emptyList()
    }
  val average =
    if (intervalCount == 0) {
      Amount.Zero
    } else {
      Amount((total.toLong().toDouble() / intervalCount).roundToLong())
    }
  return (middle + total + average).toImmutableList()
}

@Composable
private fun GroupBy.name(): String =
  when (this) {
    Category -> Strings.reportsCustomTableCategory
    Group,
    CategoryGroup -> Strings.reportsCustomTableGroup
    Payee -> Strings.reportsCustomTablePayee
    Account -> Strings.reportsCustomTableAccount
    GroupBy.Interval,
    Unknown -> ""
  }

// ReportOptions.ts intervalMap
@Composable
private fun Interval.unitName(): String =
  when (this) {
    Daily -> Strings.reportsCustomTableDay
    Weekly -> Strings.reportsCustomTableWeek
    Monthly,
    Unknown -> Strings.reportsCustomTableMonth
    Yearly -> Strings.reportsCustomTableYear
  }

private val NAME_WIDTH = 125.dp
private val COMPACT_NAME_WIDTH = 80.dp
private val AMOUNT_WIDTH = 85.dp
private val COMPACT_AMOUNT_WIDTH = 50.dp
private val ROW_HEIGHT = 32.dp
private val COMPACT_ROW_HEIGHT = 20.dp
private val GAP_HEIGHT = 20.dp
private val CELL_PADDING = 4.dp

// Totals and average
private const val BOLD_COLUMNS = 2
