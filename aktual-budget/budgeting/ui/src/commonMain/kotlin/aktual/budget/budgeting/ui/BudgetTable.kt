package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.Banner
import aktual.budget.budgeting.vm.BudgetSummary
import aktual.budget.budgeting.vm.CategoryRow
import aktual.budget.budgeting.vm.GroupRow
import aktual.budget.budgeting.vm.MonthBudget
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.core.icons.material.ArrowDropDown
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.ExpandMore
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Warning
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.formattedString
import aktual.core.ui.stringLong
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.YearMonth

// packages/desktop-client/src/components/mobile/budget/BudgetTable.tsx
internal fun LazyListScope.budgetTable(
  budget: MonthBudget,
  type: BudgetType,
  showSpent: Boolean,
  onAction: BudgetActionHandler,
) {
  item(key = "summary") {
    SummaryCard(
      summary = budget.summary,
      label = Strings.budgetingSummaryOpen(budget.month.stringLong()),
      onClick = { onAction(OpenSheet(SheetRequest.SummarySheet(budget.month))) },
      modifier = Modifier.padding(bottom = 8.dp),
    )
  }

  items(budget.banners, key = { it::class.simpleName.orEmpty() }) { banner ->
    BannerRow(
      banner = banner,
      month = budget.month,
      type = type,
      onAction = onAction,
      modifier = Modifier.padding(bottom = 8.dp),
    )
  }

  item(key = "header") { ColumnHeader(showSpent = showSpent, onAction = onAction) }

  for (group in budget.groups) {
    stickyHeader(key = "group-${group.id.value}") {
      GroupHeader(
        group = group,
        value = if (showSpent) group.spent else group.budgeted,
        onAction = onAction,
      )
    }

    if (!group.isCollapsed) {
      items(group.categories, key = { "category-${it.id.value}" }) { category ->
        CategoryItem(
          category = category,
          showSpent = showSpent,
          onBalance = { onAction(OpenSheet(SheetRequest.BalanceSheet(budget.month, category.id))) },
          onEdit = { onAction(OpenSheet(SheetRequest.EditSheet(budget.month, category.id))) },
          onOpen = { onAction(OpenCategory(budget.month, category.id)) },
        )
      }
    }
  }

  val income = budget.income ?: return
  val isTracking = type == Tracking

  item(key = "income-header") { IncomeColumnHeader(isTracking = isTracking) }

  stickyHeader(key = "group-${income.id.value}") {
    IncomeGroupHeader(group = income, isTracking = isTracking, onAction = onAction)
  }

  if (!income.isCollapsed) {
    items(income.categories, key = { "category-${it.id.value}" }) { category ->
      IncomeItem(
        category = category,
        isTracking = isTracking,
        onEdit = { onAction(OpenSheet(SheetRequest.EditSheet(budget.month, category.id))) },
        onOpen = { onAction(OpenCategory(budget.month, category.id)) },
      )
    }
  }
}

@Composable
private fun SummaryCard(
  summary: BudgetSummary,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(CardShape)
        .background(colors.tableBackground, CardShape)
        .border(1.dp, colors.tableBorder, CardShape)
        .clickable(onClickLabel = label, role = Button, onClick = onClick)
        .padding(12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = CenterVertically,
  ) {
    Headline(headline = summary.headline())
    Breakdown(lines = summary.breakdown())
  }
}

internal data class SummaryHeadline(val label: String, val amount: Amount, val color: Color)

@Composable
internal fun BudgetSummary.headline(): SummaryHeadline =
  when (this) {
    is Envelope ->
      SummaryHeadline(
        label = if (toBudget < Zero) Strings.budgetingOverbudgeted else Strings.budgetingToBudget,
        amount = toBudget,
        color =
          when {
            toBudget < Zero -> colors.toBudgetNegative
            toBudget > Zero -> colors.toBudgetPositive
            else -> colors.toBudgetZero
          },
      )

    is Tracking ->
      SummaryHeadline(
        label =
          when {
            isProjected -> Strings.budgetingProjectedSavings
            saved < Zero -> Strings.budgetingOverspent
            else -> Strings.budgetingSaved
          },
        amount = saved,
        color =
          when {
            isProjected -> colors.warningText
            saved < Zero -> colors.errorTextDark
            else -> colors.pageText
          },
      )
  }

@Composable
internal fun BudgetSummary.breakdown(): ImmutableList<Pair<String, Amount>> =
  when (this) {
    is Envelope ->
      persistentListOf(
        Strings.budgetingAvailable to available,
        Strings.budgetingBudgeted to budgeted,
      )

    is Tracking ->
      persistentListOf(Strings.budgetingBudgeted to budgeted, Strings.budgetingSpent to spent)
  }

@Composable
private fun Headline(headline: SummaryHeadline, modifier: Modifier = Modifier) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Text(text = headline.label, fontSize = 13.sp, color = colors.pageTextSubdued)
    Text(
      text = headline.amount.formattedString(includeSign = true),
      style = typography.headlineSmall.tabularFigures(),
      fontWeight = SemiBold,
      color = headline.color,
      maxLines = 1,
    )
  }
}

@Composable
private fun Breakdown(lines: ImmutableList<Pair<String, Amount>>, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.End,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    lines.fastForEach { (label, amount) ->
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = CenterVertically,
      ) {
        Text(text = label, fontSize = 13.sp, color = colors.pageTextSubdued)
        Text(
          text = amount.formattedString(),
          fontSize = 13.sp,
          style = TextStyle.Default.tabularFigures(),
          color = colors.pageText,
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
internal fun BannerRow(
  banner: Banner,
  month: YearMonth,
  type: BudgetType,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  val isWarning = banner is Uncategorised
  val background = if (isWarning) colors.warningBackground else colors.errorBackground
  val border = if (isWarning) colors.warningBorder else colors.errorBorder
  val text = if (isWarning) colors.warningText else colors.errorText

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(40.dp)
        .background(background, CardShape)
        .border(1.dp, border, CardShape)
        .padding(start = 12.dp, end = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(18.dp),
      imageVector = MaterialIcons.Warning,
      contentDescription = null,
      tint = text,
    )

    Text(
      modifier = Modifier.weight(1f),
      text =
        when (banner) {
          is Uncategorised -> Plurals.homeAttentionUncategorised(banner.count, banner.count)
          is Overspent ->
            Plurals.budgetingBannerOverspent(
              banner.count,
              banner.count,
              banner.total.formattedString(),
            )
          is Overbudgeted -> Strings.budgetingBannerOverbudgeted
        },
      fontSize = 14.sp,
      color = text,
      maxLines = 2,
      overflow = Ellipsis,
    )

    // packages/desktop-client/src/components/mobile/budget/BudgetPage.tsx OverbudgetedBanner and
    // OverspendingBanner. Tracking budgets can't cover overspending
    val action =
      when (banner) {
        is Uncategorised -> Strings.budgetingBannerReview to ReviewUncategorised
        is Overspent if type == Envelope ->
          Strings.budgetingBannerCover to OpenSheet(SheetRequest.OverspentSheet(month))
        is Overspent -> null
        is Overbudgeted ->
          Strings.budgetingBannerCover to OpenSheet(SheetRequest.CoverOverbudgetedSheet(month))
      }
    if (action != null) {
      TextButton(onClick = { onAction(action.second) }) {
        Text(text = action.first, color = text, fontWeight = SemiBold)
      }
    }
  }
}

@Composable
private fun ColumnHeader(
  showSpent: Boolean,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  TableRow(
    modifier = modifier.background(colors.tableHeaderBackground, TopShape),
    height = BudgetDS.headerHeight,
  ) {
    HeaderLabel(text = Strings.budgetingColumnCategory, modifier = Modifier.weight(1f))

    Row(
      modifier =
        Modifier.width(BudgetDS.valueWidth).clickable(
          onClickLabel =
            if (showSpent) Strings.budgetingToggleBudgeted else Strings.budgetingToggleSpent,
          role = Button,
        ) {
          onAction(ToggleSpent)
        },
      horizontalArrangement = Arrangement.End,
      verticalAlignment = CenterVertically,
    ) {
      Text(
        text = if (showSpent) Strings.budgetingColumnSpent else Strings.budgetingColumnBudgeted,
        fontSize = 13.sp,
        fontWeight = Medium,
        color = colors.pageTextLink,
      )
      Icon(
        modifier = Modifier.size(16.dp),
        imageVector = MaterialIcons.ArrowDropDown,
        contentDescription = null,
        tint = colors.pageTextLink,
      )
    }

    HeaderLabel(
      text = Strings.budgetingColumnBalance,
      modifier = Modifier.width(BudgetDS.balanceWidth),
      textAlign = End,
    )
  }
}

@Composable
private fun IncomeColumnHeader(isTracking: Boolean, modifier: Modifier = Modifier) {
  TableRow(
    modifier = modifier.padding(top = 16.dp).background(colors.tableHeaderBackground, TopShape),
    height = BudgetDS.headerHeight,
  ) {
    Spacer(modifier = Modifier.weight(1f))
    if (isTracking) {
      HeaderLabel(
        text = Strings.budgetingColumnBudgeted,
        modifier = Modifier.width(BudgetDS.valueWidth),
        textAlign = End,
      )
    }
    HeaderLabel(
      text = Strings.budgetingColumnReceived,
      modifier = Modifier.width(BudgetDS.balanceWidth),
      textAlign = End,
    )
  }
}

@Composable
internal fun HeaderLabel(
  text: String,
  modifier: Modifier = Modifier,
  textAlign: TextAlign = Start,
) =
  Text(
    modifier = modifier,
    text = text,
    fontSize = 13.sp,
    color = colors.tableHeaderText,
    textAlign = textAlign,
    maxLines = 1,
  )

@Composable
private fun GroupHeader(
  group: GroupRow,
  value: Amount,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  GroupHeaderRow(group = group, onAction = onAction, modifier = modifier) {
    AmountText(amount = value, modifier = Modifier.width(BudgetDS.valueWidth), bold = true)
    AmountText(
      amount = group.balance,
      modifier = Modifier.width(BudgetDS.balanceWidth),
      color = balanceColors(group.balance).text,
      bold = true,
    )
  }
}

@Composable
private fun IncomeGroupHeader(
  group: GroupRow,
  isTracking: Boolean,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
) {
  GroupHeaderRow(group = group, onAction = onAction, modifier = modifier) {
    if (isTracking) {
      AmountText(
        amount = group.budgeted,
        modifier = Modifier.width(BudgetDS.valueWidth),
        bold = true,
      )
    }
    AmountText(amount = group.spent, modifier = Modifier.width(BudgetDS.balanceWidth), bold = true)
  }
}

// Without a name width, the name takes the space the amounts leave
@Composable
internal fun GroupHeaderRow(
  group: GroupRow,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
  nameWidth: Dp? = null,
  amounts: @Composable RowScope.() -> Unit,
) {
  val label =
    if (group.isCollapsed) {
      Strings.budgetingGroupExpand(group.name)
    } else {
      Strings.budgetingGroupCollapse(group.name)
    }

  Column(modifier = modifier.fillMaxWidth().background(colors.tableRowHeaderBackground)) {
    HorizontalDivider(color = colors.tableBorder)
    TableRow(
      modifier =
        Modifier.alpha(if (group.isHidden) HIDDEN_ALPHA else 1f).clickable(
          onClickLabel = label,
          role = Button,
        ) {
          onAction(ToggleGroup(group.id))
        },
      height = BudgetDS.headerHeight,
    ) {
      Row(
        modifier = if (nameWidth == null) Modifier.weight(1f) else Modifier.width(nameWidth),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = CenterVertically,
      ) {
        Icon(
          modifier = Modifier.size(16.dp).rotate(if (group.isCollapsed) COLLAPSED_ROTATION else 0f),
          imageVector = MaterialIcons.ExpandMore,
          contentDescription = null,
          tint = colors.tableRowHeaderText,
        )
        Text(
          text = group.name,
          fontSize = 14.sp,
          fontWeight = SemiBold,
          color = colors.tableRowHeaderText,
          maxLines = 1,
          overflow = Ellipsis,
        )
      }
      amounts()
    }
  }
}

// Only the budgeted amount can be edited, so Spent isn't tappable
@Composable
private fun CategoryItem(
  category: CategoryRow,
  showSpent: Boolean,
  onEdit: () -> Unit,
  onBalance: () -> Unit,
  onOpen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  CategoryRowLayout(category = category, onOpen = onOpen, modifier = modifier) {
    if (showSpent) {
      AmountText(amount = category.spent, modifier = Modifier.width(BudgetDS.valueWidth))
    } else {
      EditableAmount(amount = category.budgeted, name = category.name, onEdit = onEdit)
    }
    val balanceLabel = Strings.budgetingBalanceOptions(category.name)
    Box(
      modifier =
        Modifier.width(BudgetDS.balanceWidth)
          .height(BudgetDS.rowHeight)
          .clickable(onClickLabel = balanceLabel, role = Button, onClick = onBalance),
      contentAlignment = CenterEnd,
    ) {
      BalancePill(balance = category.balance, carryover = category.carryover)
    }
  }
}

// Envelope budgets don't budget income, as upstream shows Received only
@Composable
private fun IncomeItem(
  category: CategoryRow,
  isTracking: Boolean,
  onEdit: () -> Unit,
  onOpen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  CategoryRowLayout(category = category, onOpen = onOpen, modifier = modifier) {
    if (isTracking) {
      EditableAmount(amount = category.budgeted, name = category.name, onEdit = onEdit)
    }
    AmountText(
      amount = category.spent,
      modifier = Modifier.width(BudgetDS.balanceWidth),
      color = if (category.spent > Zero) colors.numberPositive else colors.tableText,
      includeSign = true,
    )
  }
}

@Composable
private fun EditableAmount(amount: Amount, name: String, onEdit: () -> Unit) {
  val label = Strings.budgetingEditBudget(name)
  Box(
    modifier =
      Modifier.width(BudgetDS.valueWidth)
        .height(BudgetDS.rowHeight)
        .clickable(onClickLabel = label, role = Button, onClick = onEdit),
    contentAlignment = CenterEnd,
  ) {
    AmountText(amount = amount)
  }
}

@Composable
internal fun CategoryRowLayout(
  category: CategoryRow,
  onOpen: () -> Unit,
  modifier: Modifier = Modifier,
  nameWidth: Dp? = null,
  amounts: @Composable RowScope.() -> Unit,
) {
  val openLabel = Strings.budgetingCategoryOpen(category.name)
  Column(modifier = modifier.fillMaxWidth().background(colors.tableBackground)) {
    HorizontalDivider(color = colors.tableBorder)
    TableRow(
      modifier =
        Modifier.alpha(if (category.isHidden) HIDDEN_ALPHA else 1f).padding(start = CategoryInset),
      height = BudgetDS.rowHeight,
    ) {
      Text(
        modifier =
          (if (nameWidth == null) {
              Modifier.weight(1f)
            } else {
              Modifier.width(nameWidth - CategoryInset)
            })
            .clickable(onClickLabel = openLabel, role = Button, onClick = onOpen),
        text = category.name,
        fontSize = 14.sp,
        color = colors.tableText,
        maxLines = 1,
        overflow = Ellipsis,
      )
      amounts()
    }
  }
}

@Composable
internal fun TableRow(
  height: Dp,
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) =
  Row(
    modifier = modifier.fillMaxWidth().height(height).padding(horizontal = BudgetDS.rowPadding),
    verticalAlignment = CenterVertically,
    content = content,
  )

@Composable
internal fun AmountText(
  amount: Amount,
  modifier: Modifier = Modifier,
  color: Color = colors.tableText,
  bold: Boolean = false,
  includeSign: Boolean = false,
) =
  Text(
    modifier = modifier,
    text = amount.formattedString(includeSign = includeSign),
    fontSize = 14.sp,
    fontWeight = if (bold) SemiBold else Normal,
    style = TextStyle.Default.tabularFigures(),
    color = color,
    textAlign = End,
    maxLines = 1,
  )

// packages/desktop-client/src/components/budget/BalanceWithCarryover.tsx
@Composable
internal fun BalancePill(balance: Amount, carryover: Boolean, modifier: Modifier = Modifier) {
  val pill = balanceColors(balance)
  val rollsOver = Strings.budgetingRollsOver
  Row(
    modifier =
      modifier
        .background(pill.background, PillShape)
        .padding(horizontal = 8.dp, vertical = 3.dp)
        .semantics { if (carryover) stateDescription = rollsOver },
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      text = balance.formattedString(includeSign = true),
      fontSize = 13.sp,
      fontWeight = Medium,
      style = TextStyle.Default.tabularFigures(),
      color = pill.text,
      maxLines = 1,
    )
    if (carryover) {
      Icon(
        modifier = Modifier.size(12.dp),
        imageVector = MaterialIcons.ArrowRight,
        contentDescription = null,
        tint = pill.text,
      )
    }
  }
}

internal data class PillColors(val background: Color, val text: Color)

@Composable
@ReadOnlyComposable
internal fun balanceColors(balance: Amount): PillColors =
  when {
    balance > Zero -> PillColors(colors.noticeBackground, colors.noticeText)
    balance < Zero -> PillColors(colors.errorBackground, colors.errorText)
    else -> PillColors(colors.pillBackground, colors.pillText)
  }

// Keep in sync with budgetTable
@Composable
internal fun ShimmerBudgetTable(modifier: Modifier = Modifier) {
  val bar = Modifier.background(colors.tableText, CardShape)
  Column(
    modifier = modifier.fillMaxWidth().shimmer(rememberShimmer(Window)),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(modifier = bar.fillMaxWidth().height(72.dp))
    repeat(times = SHIMMER_GROUPS) {
      Box(modifier = bar.fillMaxWidth().height(BudgetDS.headerHeight))
      repeat(times = SHIMMER_CATEGORIES) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(start = BudgetDS.categoryIndent),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = CenterVertically,
        ) {
          Box(modifier = bar.weight(1f).height(20.dp))
          Box(modifier = bar.width(72.dp).height(20.dp))
          Box(modifier = bar.width(64.dp).height(20.dp))
        }
      }
    }
  }
}

internal fun TextStyle.tabularFigures() = copy(fontFeatureSettings = "tnum")

internal val TopShape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
private val CategoryInset = BudgetDS.categoryIndent - BudgetDS.rowPadding
private val PillShape = RoundedCornerShape(12.dp)
private const val HIDDEN_ALPHA = 0.5f
private const val COLLAPSED_ROTATION = -90f
private const val SHIMMER_GROUPS = 3
private const val SHIMMER_CATEGORIES = 3
