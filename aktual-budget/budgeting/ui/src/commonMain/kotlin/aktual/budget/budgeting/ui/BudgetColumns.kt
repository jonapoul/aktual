package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.BudgetState
import aktual.budget.budgeting.vm.CategoryRow
import aktual.budget.budgeting.vm.GroupRow
import aktual.budget.budgeting.vm.MonthBudget
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.formattedString
import aktual.core.ui.scrollbar
import aktual.core.ui.stringLong
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus

// How many month columns fit next to the category names.
// packages/desktop-client/src/components/budget/DynamicBudgetTable.tsx getNumPossibleMonths
internal fun fittingMonths(width: Dp): Int =
  ((width - ColumnsDS.chrome) / ColumnsDS.monthWidth).toInt().coerceIn(1, MAX_MONTHS)

// packages/desktop-client/src/components/budget/BudgetTable.tsx. The category names stay put while
// the months scroll sideways together
@Composable
internal fun BudgetColumns(
  state: BudgetState.Loaded,
  listState: LazyListState,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  val columns = remember(state.months, state.month, state.monthCount) { state.columns() }
  val scroll = rememberScrollState()
  val cursor = remember { BudgetCursor() }
  val grid = remember(columns, state.type) { columns.grid(isTracking = state.type == Tracking) }
  val tableFocus = remember { FocusRequester() }

  // Back to the table once an edit ends, so the arrow keys keep working
  LaunchedEffect(cursor.isEditing, cursor.selected) {
    if (!cursor.isEditing && cursor.selected != null) tableFocus.requestFocus()
  }

  // The column header and a group header stick to the top
  val stickyHeight = with(LocalDensity.current) { (BudgetDS.headerHeight * 2).roundToPx() }
  LaunchedEffect(cursor.selected?.category, columns) {
    val category = cursor.selected?.category ?: return@LaunchedEffect
    val index = columns.itemKeys().indexOf(categoryKey(category))
    if (index >= 0) listState.reveal(index, stickyHeight)
  }

  BoxWithConstraints(
    modifier =
      modifier
        .testTag(BudgetTags.Columns)
        .focusRequester(tableFocus)
        .onKeyEvent { event -> event.moveCursor(cursor, grid) }
        .focusable(),
  ) {
    val layout =
      ColumnsLayout(
        monthWidth = maxOf(ColumnsDS.monthWidth, (maxWidth - ColumnsDS.chrome) / columns.size),
        scroll = scroll,
      )

    LazyColumn(
      modifier = Modifier.scrollbar(listState),
      state = listState,
      contentPadding = BudgetDS.listPadding,
    ) {
      budgetColumns(state, columns, layout, cursor, grid, onAction)
      item(key = "bottom") { BottomSpacing() }
    }
  }
}

private fun LazyListScope.budgetColumns(
  state: BudgetState.Loaded,
  columns: ImmutableList<MonthColumn>,
  layout: ColumnsLayout,
  cursor: BudgetCursor,
  grid: BudgetGrid,
  onAction: BudgetActionHandler,
) {
  item(key = "summaries") {
    Summaries(state = state, columns = columns, layout = layout, onAction = onAction)
  }

  stickyHeader(key = "header") { ColumnHeaders(columns = columns, layout = layout) }

  // Every month has the same groups and categories
  val template = columns.firstNotNullOfOrNull { it.budget } ?: return
  val isTracking = state.type == Tracking

  for (group in template.groups) {
    stickyHeader(key = "group-${group.id.value}") {
      GroupHeaderRow(group = group, onAction = onAction, nameWidth = ColumnsDS.categoryWidth) {
        Months(columns, layout) { column ->
          val month = column.groups[group.id] ?: return@Months
          AmountCell(amount = month.budgeted, bold = true)
          AmountCell(amount = month.spent, bold = true)
          AmountCell(amount = month.balance, color = balanceColors(month.balance).text, bold = true)
        }
      }
    }

    if (!group.isCollapsed) {
      items(group.categories, key = { categoryKey(it.id) }) { category ->
        CategoryRowLayout(category = category, nameWidth = ColumnsDS.categoryWidth) {
          Months(columns, layout) { column ->
            val month = column.categories[category.id] ?: return@Months
            BudgetedCell(
              cell = BudgetCell(column.month, category.id),
              amount = month.budgeted,
              name = category.name,
              type = state.type,
              cursor = cursor,
              grid = grid,
              onAction = onAction,
            )
            AmountCell(
              amount = month.spent,
              color = if (month.spent == Zero) colors.pageTextSubdued else colors.tableText,
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = CenterEnd) {
              BalancePill(balance = month.balance, carryover = month.carryover)
            }
          }
        }
      }
    }
  }

  val income = template.income ?: return

  stickyHeader(key = "group-${income.id.value}") {
    GroupHeaderRow(group = income, onAction = onAction, nameWidth = ColumnsDS.categoryWidth) {
      Months(columns, layout) { column ->
        val month = column.groups[income.id] ?: return@Months
        IncomeCells(
          budgeted = month.budgeted,
          received = month.spent,
          isTracking = isTracking,
          bold = true,
        )
      }
    }
  }

  if (!income.isCollapsed) {
    items(income.categories, key = { categoryKey(it.id) }) { category ->
      CategoryRowLayout(category = category, nameWidth = ColumnsDS.categoryWidth) {
        Months(columns, layout) { column ->
          val month = column.categories[category.id] ?: return@Months
          // Envelope budgets don't budget income, as upstream shows Received only
          if (isTracking) {
            BudgetedCell(
              cell = BudgetCell(column.month, category.id),
              amount = month.budgeted,
              name = category.name,
              type = state.type,
              cursor = cursor,
              grid = grid,
              onAction = onAction,
            )
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }
          IncomeReceived(received = month.spent, bold = false)
        }
      }
    }
  }
}

// packages/desktop-client/src/components/budget/BudgetSummaries.tsx
@Composable
private fun Summaries(
  state: BudgetState.Loaded,
  columns: ImmutableList<MonthColumn>,
  layout: ColumnsLayout,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  val uncategorised = columns.firstNotNullOfOrNull { column ->
    column.budget?.banners?.firstOrNull { it is Uncategorised }
  }

  Row(
    modifier =
      modifier.fillMaxWidth().padding(horizontal = BudgetDS.rowPadding).padding(bottom = 8.dp),
    verticalAlignment = Bottom,
  ) {
    Box(modifier = Modifier.width(ColumnsDS.categoryWidth).padding(end = ColumnsDS.monthGap)) {
      if (uncategorised != null) BannerRow(banner = uncategorised, onAction = onAction)
    }

    Row(modifier = Modifier.weight(1f).horizontalScroll(layout.scroll)) {
      columns.fastForEach { column ->
        SummaryCard(
          modifier = Modifier.width(layout.monthWidth).padding(start = ColumnsDS.monthGap),
          column = column,
          isCurrent = column.month == state.current,
        )
      }
    }
  }
}

@Composable
private fun SummaryCard(column: MonthColumn, isCurrent: Boolean, modifier: Modifier = Modifier) {
  Column(
    modifier =
      modifier
        .background(colors.tableBackground, CardShape)
        .border(
          width = if (isCurrent) 2.dp else 1.dp,
          color = if (isCurrent) colors.pageTextLink else colors.tableBorder,
          shape = CardShape,
        )
        .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    SummaryLine {
      Text(
        text = column.month.stringLong(),
        fontSize = 15.sp,
        fontWeight = SemiBold,
        color = colors.pageText,
        maxLines = 1,
      )
      if (isCurrent) {
        Text(
          text = Strings.budgetingThisMonth,
          fontSize = 12.sp,
          fontWeight = Medium,
          color = colors.pageTextLink,
          maxLines = 1,
        )
      }
    }

    val summary = column.budget?.summary ?: return@Column

    summary.breakdown().fastForEach { (label, amount) ->
      SummaryLine {
        Text(text = label, fontSize = 13.sp, color = colors.pageTextSubdued, maxLines = 1)
        SummaryAmount(amount = amount, includeSign = false, fontSize = 13, color = colors.pageText)
      }
    }

    HorizontalDivider(color = colors.tableBorder)

    val headline = summary.headline()
    SummaryLine {
      Text(text = headline.label, fontSize = 13.sp, color = colors.pageTextSubdued, maxLines = 1)
      SummaryAmount(
        amount = headline.amount,
        includeSign = true,
        fontSize = 18,
        color = headline.color,
      )
    }
  }
}

@Composable
private fun SummaryLine(content: @Composable RowScope.() -> Unit) =
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = CenterVertically,
    content = content,
  )

@Composable
private fun SummaryAmount(
  amount: Amount,
  includeSign: Boolean,
  fontSize: Int,
  color: Color,
) =
  Text(
    text = amount.formattedString(includeSign = includeSign),
    fontSize = fontSize.sp,
    fontWeight = if (includeSign) SemiBold else Normal,
    style = TextStyle.Default.tabularFigures(),
    color = color,
    maxLines = 1,
  )

@Composable
private fun ColumnHeaders(
  columns: ImmutableList<MonthColumn>,
  layout: ColumnsLayout,
  modifier: Modifier = Modifier,
) {
  TableRow(
    modifier = modifier.background(colors.tableHeaderBackground, TopShape),
    height = BudgetDS.headerHeight,
  ) {
    HeaderLabel(
      text = Strings.budgetingColumnCategory,
      modifier = Modifier.width(ColumnsDS.categoryWidth),
    )
    Months(columns, layout) {
      HeaderCell(text = Strings.budgetingColumnBudgeted)
      HeaderCell(text = Strings.budgetingColumnSpent)
      HeaderCell(text = Strings.budgetingColumnBalance)
    }
  }
}

// One set of cells per month. Nothing is shown for a month that hasn't loaded
@Composable
private fun RowScope.Months(
  columns: ImmutableList<MonthColumn>,
  layout: ColumnsLayout,
  cells: @Composable RowScope.(MonthColumn) -> Unit,
) {
  Row(modifier = Modifier.weight(1f).horizontalScroll(layout.scroll)) {
    columns.fastForEach { column ->
      Row(
        modifier = Modifier.width(layout.monthWidth).padding(start = ColumnsDS.monthGap),
        verticalAlignment = CenterVertically,
      ) {
        cells(column)
      }
    }
  }
}

@Composable
private fun RowScope.HeaderCell(text: String) =
  HeaderLabel(text = text, modifier = Modifier.weight(1f), textAlign = End)

@Composable
private fun RowScope.AmountCell(
  amount: Amount,
  color: Color = colors.tableText,
  bold: Boolean = false,
  includeSign: Boolean = false,
) =
  AmountText(
    amount = amount,
    modifier = Modifier.weight(1f),
    color = color,
    bold = bold,
    includeSign = includeSign,
  )

// Income is budgeted for in tracking budgets only, and what's received sits under Balance
@Composable
private fun RowScope.IncomeCells(
  budgeted: Amount,
  received: Amount,
  isTracking: Boolean,
  bold: Boolean,
) {
  if (isTracking) {
    AmountCell(amount = budgeted, bold = bold)
  } else {
    Spacer(modifier = Modifier.weight(1f))
  }
  IncomeReceived(received = received, bold = bold)
}

@Composable
private fun RowScope.IncomeReceived(received: Amount, bold: Boolean) {
  Spacer(modifier = Modifier.weight(1f))
  AmountCell(
    amount = received,
    color = if (received > Zero) colors.numberPositive else colors.tableText,
    bold = bold,
    includeSign = true,
  )
}

@Immutable
private data class MonthColumn(
  val month: YearMonth,
  val budget: MonthBudget?,
  val groups: Map<CategoryGroupId, GroupRow>,
  val categories: Map<CategoryId, CategoryRow>,
)

@Immutable private data class ColumnsLayout(val monthWidth: Dp, val scroll: ScrollState)

private fun categoryKey(id: CategoryId) = "category-${id.value}"

// The item keys budgetColumns lays out, to find a row's index while it's off screen
private fun List<MonthColumn>.itemKeys(): List<String> = buildList {
  add("summaries")
  add("header")
  val template = this@itemKeys.firstNotNullOfOrNull { it.budget } ?: return@buildList
  for (group in template.groups + listOfNotNull(template.income)) {
    add("group-${group.id.value}")
    if (!group.isCollapsed) group.categories.forEach { add(categoryKey(it.id)) }
  }
}

// Rows that can be edited: expense categories, and income ones in tracking budgets
private fun List<MonthColumn>.grid(isTracking: Boolean): BudgetGrid {
  val template = firstNotNullOfOrNull { it.budget }
  val groups = template?.groups.orEmpty() + listOfNotNull(template?.income?.takeIf { isTracking })
  return BudgetGrid(
    months = map { it.month }.toImmutableList(),
    rows =
      groups
        .asSequence()
        .filter { !it.isCollapsed }
        .flatMap { it.categories }
        .map { it.id }
        .toImmutableList(),
  )
}

// Scrolls just enough to show the row below the sticky headers, jumping first if it's off screen
private suspend fun LazyListState.reveal(index: Int, stickyHeight: Int) {
  if (layoutInfo.visibleItemsInfo.none { it.index == index }) {
    scrollToItem(index)
    scrollBy(-stickyHeight.toFloat())
  }
  val info = layoutInfo
  val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return
  val top = info.viewportStartOffset + stickyHeight
  val bottom = info.viewportEndOffset - info.afterContentPadding
  when {
    item.offset < top -> animateScrollBy((item.offset - top).toFloat())
    item.offset + item.size > bottom ->
      animateScrollBy((item.offset + item.size - bottom).toFloat())
  }
}

private fun BudgetState.Loaded.columns(): ImmutableList<MonthColumn> =
  List(monthCount) { index ->
      val shown = month.plus(index, MONTH)
      val budget = this[shown]
      val groups = budget?.run { groups + listOfNotNull(income) }.orEmpty()
      MonthColumn(
        month = shown,
        budget = budget,
        groups = groups.associateBy { it.id },
        categories = groups.flatMap { it.categories }.associateBy { it.id },
      )
    }
    .toImmutableList()

private object ColumnsDS {
  val categoryWidth = 220.dp
  val monthWidth = 280.dp
  val monthGap = 8.dp

  // Everything beside the months: list and row padding, and the category names
  val chrome = categoryWidth + (BudgetDS.rowPadding + 8.dp) * 2
}

private const val MAX_MONTHS = 4
