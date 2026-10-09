package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.Banner
import aktual.budget.budgeting.vm.BudgetState
import aktual.budget.budgeting.vm.BudgetSummary
import aktual.budget.budgeting.vm.CategoryRow
import aktual.budget.budgeting.vm.GroupRow
import aktual.budget.model.Amount
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

// packages/desktop-client/src/components/mobile/budget/BudgetTable.tsx
internal fun LazyListScope.budgetTable(state: BudgetState.Loaded, onAction: BudgetActionHandler) {
  item(key = "summary") {
    SummaryCard(summary = state.summary, modifier = Modifier.padding(bottom = 8.dp))
  }

  items(state.banners, key = { it::class.simpleName.orEmpty() }) { banner ->
    BannerRow(banner = banner, onAction = onAction, modifier = Modifier.padding(bottom = 8.dp))
  }

  item(key = "header") { ColumnHeader(showSpent = state.showSpent, onAction = onAction) }

  for (group in state.groups) {
    stickyHeader(key = "group-${group.id.value}") {
      GroupHeader(
        group = group,
        value = if (state.showSpent) group.spent else group.budgeted,
        onAction = onAction,
      )
    }

    if (!group.isCollapsed) {
      items(group.categories, key = { "category-${it.id.value}" }) { category ->
        CategoryItem(category = category, showSpent = state.showSpent)
      }
    }
  }

  val income = state.income ?: return
  val isTracking = state.type == Tracking

  item(key = "income-header") { IncomeColumnHeader(isTracking = isTracking) }

  stickyHeader(key = "group-${income.id.value}") {
    IncomeGroupHeader(group = income, isTracking = isTracking, onAction = onAction)
  }

  if (!income.isCollapsed) {
    items(income.categories, key = { "category-${it.id.value}" }) { category ->
      IncomeItem(category = category, isTracking = isTracking)
    }
  }
}

@Composable
private fun SummaryCard(summary: BudgetSummary, modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.tableBackground, CardShape)
        .border(1.dp, colors.tableBorder, CardShape)
        .padding(12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = CenterVertically,
  ) {
    when (summary) {
      is Envelope -> {
        Headline(
          label =
            if (summary.toBudget < Zero) {
              Strings.budgetingOverbudgeted
            } else {
              Strings.budgetingToBudget
            },
          amount = summary.toBudget,
          color =
            when {
              summary.toBudget < Zero -> colors.toBudgetNegative
              summary.toBudget > Zero -> colors.toBudgetPositive
              else -> colors.toBudgetZero
            },
        )
        Breakdown(
          Strings.budgetingAvailable to summary.available,
          Strings.budgetingBudgeted to summary.budgeted,
        )
      }

      is Tracking -> {
        Headline(
          label =
            when {
              summary.isProjected -> Strings.budgetingProjectedSavings
              summary.saved < Zero -> Strings.budgetingOverspent
              else -> Strings.budgetingSaved
            },
          amount = summary.saved,
          color =
            when {
              summary.isProjected -> colors.warningText
              summary.saved < Zero -> colors.errorTextDark
              else -> colors.pageText
            },
        )
        Breakdown(
          Strings.budgetingBudgeted to summary.budgeted,
          Strings.budgetingSpent to summary.spent,
        )
      }
    }
  }
}

@Composable
private fun Headline(label: String, amount: Amount, color: Color, modifier: Modifier = Modifier) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Text(text = label, fontSize = 13.sp, color = colors.pageTextSubdued)
    Text(
      text = amount.formattedString(includeSign = true),
      style = typography.headlineSmall.tabularFigures(),
      fontWeight = SemiBold,
      color = color,
      maxLines = 1,
    )
  }
}

@Composable
private fun Breakdown(vararg lines: Pair<String, Amount>, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.End,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    for ((label, amount) in lines) {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

// Covering overspending and overbudgeting lands with the budget actions
@Composable
private fun BannerRow(
  banner: Banner,
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

    if (banner is Uncategorised) {
      TextButton(onClick = { onAction(ReviewUncategorised) }) {
        Text(text = Strings.budgetingBannerReview, color = text, fontWeight = SemiBold)
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
private fun HeaderLabel(
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

@Composable
private fun GroupHeaderRow(
  group: GroupRow,
  onAction: BudgetActionHandler,
  modifier: Modifier = Modifier,
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
        modifier = Modifier.weight(1f),
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

@Composable
private fun CategoryItem(category: CategoryRow, showSpent: Boolean, modifier: Modifier = Modifier) {
  CategoryRowLayout(category = category, modifier = modifier) {
    AmountText(
      amount = if (showSpent) category.spent else category.budgeted,
      modifier = Modifier.width(BudgetDS.valueWidth),
    )
    Box(modifier = Modifier.width(BudgetDS.balanceWidth), contentAlignment = CenterEnd) {
      BalancePill(balance = category.balance, carryover = category.carryover)
    }
  }
}

@Composable
private fun IncomeItem(category: CategoryRow, isTracking: Boolean, modifier: Modifier = Modifier) {
  CategoryRowLayout(category = category, modifier = modifier) {
    if (isTracking) {
      AmountText(amount = category.budgeted, modifier = Modifier.width(BudgetDS.valueWidth))
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
private fun CategoryRowLayout(
  category: CategoryRow,
  modifier: Modifier = Modifier,
  amounts: @Composable RowScope.() -> Unit,
) {
  Column(modifier = modifier.fillMaxWidth().background(colors.tableBackground)) {
    HorizontalDivider(color = colors.tableBorder)
    TableRow(
      modifier =
        Modifier.alpha(if (category.isHidden) HIDDEN_ALPHA else 1f)
          .padding(start = BudgetDS.categoryIndent - BudgetDS.rowPadding),
      height = BudgetDS.rowHeight,
    ) {
      Text(
        modifier = Modifier.weight(1f),
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
private fun TableRow(
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
private fun AmountText(
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
private fun BalancePill(balance: Amount, carryover: Boolean, modifier: Modifier = Modifier) {
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

private data class PillColors(val background: Color, val text: Color)

@Composable
private fun balanceColors(balance: Amount): PillColors =
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

private fun TextStyle.tabularFigures() = copy(fontFeatureSettings = "tnum")

private val TopShape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
private val PillShape = RoundedCornerShape(12.dp)
private const val HIDDEN_ALPHA = 0.5f
private const val COLLAPSED_ROTATION = -90f
private const val SHIMMER_GROUPS = 3
private const val SHIMMER_CATEGORIES = 3
