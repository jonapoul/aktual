package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.BudgetCategoryState
import aktual.budget.budgeting.vm.BudgetCategoryViewModel
import aktual.budget.budgeting.vm.CategoryHistoryMonth
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.formattedText
import aktual.core.ui.hazedTopBar
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.stringLong
import aktual.core.ui.stringShort
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlin.math.abs
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus

@Composable
internal fun BudgetCategoryScreen(
  category: CategoryId,
  month: YearMonth,
  back: BackNavigator,
  transactions: TransactionsNavigator,
  modifier: Modifier = Modifier,
  viewModel: BudgetCategoryViewModel = budgetCategoryViewModel(category, month),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  BudgetCategoryScaffold(
    modifier = modifier,
    state = state,
    onBack = { back() },
    onSelect = viewModel::select,
    onViewTransactions = { selected -> transactions(category, selected) },
  )
}

@Composable
private fun budgetCategoryViewModel(category: CategoryId, month: YearMonth) =
  assistedMetroViewModel<BudgetCategoryViewModel, BudgetCategoryViewModel.Factory>(
    key = "$category-$month",
  ) {
    create(category, month)
  }

@Composable
private fun BudgetCategoryScaffold(
  state: BudgetCategoryState,
  onBack: () -> Unit,
  onSelect: (YearMonth) -> Unit,
  onViewTransactions: (YearMonth) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val scroll = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scroll.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = onBack) },
        title = {
          val loaded = state as? BudgetCategoryState.Loaded
          Column {
            Text(
              text = loaded?.name ?: Strings.budgetingTitle,
              maxLines = 1,
              overflow = Ellipsis,
            )
            if (!loaded?.group.isNullOrEmpty()) {
              Text(
                text = loaded.group,
                fontSize = 13.sp,
                color = colors.pageTextSubdued,
                maxLines = 1,
                overflow = Ellipsis,
              )
            }
          }
        },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      when (state) {
        Loading -> Unit

        Failed ->
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.budgetingCategoryFailure,
            reason = null,
            background = colors.tableBackground,
            action = null,
          )

        is Loaded ->
          Column(
            modifier =
              Modifier.fillMaxSize()
                .verticalScroll(scroll)
                .padding(innerPadding)
                .padding(horizontal = CategoryDS.padding),
            verticalArrangement = Arrangement.spacedBy(CategoryDS.spacing),
          ) {
            state.selectedMonth?.let { selected ->
              MonthCard(month = selected, isIncome = state.isIncome)
            }

            HistoryCard(state = state, onSelect = onSelect)

            PrimaryTextButton(
              modifier = Modifier.fillMaxWidth(),
              text = Strings.budgetingCategoryTransactions,
              onClick = { onViewTransactions(state.selected) },
            )

            BottomSpacing()
          }
      }
    }
  }
}

@Composable
private fun MonthCard(
  month: CategoryHistoryMonth,
  isIncome: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().card().padding(CategoryDS.cardPadding),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = month.month.stringLong(),
      fontSize = 15.sp,
      fontWeight = SemiBold,
      color = colors.pageText,
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Figure(label = Strings.budgetingBudgeted, amount = month.budgeted)
      Figure(
        label = if (isIncome) Strings.budgetingColumnReceived else Strings.budgetingSpent,
        amount = month.spent,
      )
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = Strings.budgetingColumnBalance,
          fontSize = 13.sp,
          color = colors.pageTextSubdued,
        )
        BalancePill(balance = month.balance, carryover = month.carryover)
      }
    }
  }
}

@Composable
private fun Figure(label: String, amount: Amount, modifier: Modifier = Modifier) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Text(text = label, fontSize = 13.sp, color = colors.pageTextSubdued)
    Text(
      text = amount.formattedText(),
      fontSize = 16.sp,
      fontWeight = Medium,
      style = TextStyle.Default.tabularFigures(),
      color = colors.pageText,
    )
  }
}

// Budgeted and spent side by side per month, with the balance as a dot. Tapping a month shows it
@Composable
private fun HistoryCard(
  state: BudgetCategoryState.Loaded,
  onSelect: (YearMonth) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().card().padding(CategoryDS.cardPadding),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = Strings.budgetingCategoryHistory,
      fontSize = 15.sp,
      fontWeight = SemiBold,
      color = colors.pageText,
    )

    Legend(isIncome = state.isIncome)

    HistoryChart(
      modifier = Modifier.fillMaxWidth().height(CategoryDS.chartHeight),
      history = state.history,
      selected = state.selected,
      onSelect = onSelect,
    )

    Row(modifier = Modifier.fillMaxWidth()) {
      for (point in state.history) {
        val isSelected = point.month == state.selected
        val label = Strings.budgetingCategorySelectMonth(point.month.stringLong())
        Text(
          modifier =
            Modifier.weight(1f).semantics {
              role = Tab
              selected = isSelected
              contentDescription = label
              onClick {
                onSelect(point.month)
                true
              }
            },
          text = point.month.stringShort().take(MONTH_LABEL_LENGTH),
          fontSize = 11.sp,
          fontWeight = if (isSelected) SemiBold else Normal,
          color = if (isSelected) colors.pageText else colors.pageTextSubdued,
          textAlign = Center,
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun Legend(isIncome: Boolean, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    LegendItem(color = colors.pageTextLink, label = Strings.budgetingBudgeted)
    LegendItem(
      color = colors.pageTextSubdued,
      label = if (isIncome) Strings.budgetingColumnReceived else Strings.budgetingSpent,
    )
    LegendItem(color = colors.noticeText, label = Strings.budgetingColumnBalance)
  }
}

@Composable
private fun LegendItem(color: Color, label: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = CenterVertically) {
    Spacer(modifier = Modifier.size(8.dp).background(color, CircleShape))
    Text(text = label, fontSize = 12.sp, color = colors.pageTextSubdued)
  }
}

@Composable
private fun HistoryChart(
  history: ImmutableList<CategoryHistoryMonth>,
  selected: YearMonth,
  onSelect: (YearMonth) -> Unit,
  modifier: Modifier = Modifier,
) {
  val budgetedColor = colors.pageTextLink
  val spentColor = colors.pageTextSubdued
  val highlight = colors.tableRowBackgroundHover
  val axis = colors.tableBorder
  val positive = colors.noticeText
  val negative = colors.errorText

  Canvas(
    modifier =
      modifier.pointerInput(history) {
        detectTapGestures { offset ->
          val slot = size.width / history.size.coerceAtLeast(1)
          history.getOrNull((offset.x / slot).toInt())?.let { onSelect(it.month) }
        }
      },
  ) {
    if (history.isEmpty()) return@Canvas
    val top = history.maxOf {
      maxOf(it.budgeted.cents(), abs(it.spent.cents()), it.balance.cents())
    }
    val bottom = minOf(0L, history.minOf { it.balance.cents() })
    val range = (top - bottom).coerceAtLeast(1L).toFloat()
    fun y(cents: Long) = size.height * (top - cents) / range
    val zero = y(0L)
    val slot = size.width / history.size
    val barWidth = slot * BAR_FRACTION
    val radius = CornerRadius(2.dp.toPx())

    history.forEachIndexed { index, point ->
      val left = slot * index
      if (point.month == selected) {
        drawRoundRect(highlight, Offset(left, 0f), Size(slot, size.height), radius)
      }

      val budgetedTop = y(point.budgeted.cents().coerceAtLeast(0L))
      val spentTop = y(abs(point.spent.cents()))
      val start = left + (slot - barWidth * 2) / 2
      drawRoundRect(
        color = budgetedColor,
        topLeft = Offset(start, budgetedTop),
        size = Size(barWidth, zero - budgetedTop),
        cornerRadius = radius,
      )
      drawRoundRect(
        color = spentColor,
        topLeft = Offset(start + barWidth, spentTop),
        size = Size(barWidth, zero - spentTop),
        cornerRadius = radius,
      )

      val balance = point.balance.cents()
      drawCircle(
        color = if (balance < 0) negative else positive,
        radius = 3.dp.toPx(),
        center = Offset(left + slot / 2, y(balance)),
      )
    }

    drawLine(axis, Offset(0f, zero), Offset(size.width, zero), strokeWidth = 1.dp.toPx())
  }
}

@Composable
@ReadOnlyComposable
private fun Modifier.card(): Modifier =
  background(colors.tableBackground, CardShape).border(1.dp, colors.tableBorder, CardShape)

private fun Amount.cents(): Long = toLong()

private const val BAR_FRACTION = 0.3f
private const val MONTH_LABEL_LENGTH = 3

private object CategoryDS {
  val padding = 16.dp
  val spacing = 12.dp
  val cardPadding = 12.dp
  val chartHeight = 160.dp
}

private val PREVIEW_MONTH = YearMonth(2026, 10)
private val PREVIEW_EARLIER_MONTH = YearMonth(2026, 7)
private val PREVIEW_BUDGETED = Amount(35_000L)
private val PREVIEW_SPENT =
  listOf(31_000L, 28_500L, 36_200L, 33_000L, 29_900L, 41_000L, 34_400L, 30_100L, 35_000L, 27_800L)

private val PREVIEW_HISTORY =
  PREVIEW_SPENT.mapIndexed { index, spent ->
      CategoryHistoryMonth(
        month = PREVIEW_MONTH.minus(PREVIEW_SPENT.lastIndex - index, MONTH),
        budgeted = PREVIEW_BUDGETED,
        spent = -Amount(spent),
        balance = PREVIEW_BUDGETED - Amount(spent),
        carryover = index == 0,
      )
    }
    .toImmutableList()

private val PREVIEW_CATEGORY =
  BudgetCategoryState.Loaded(
    type = Envelope,
    name = "Groceries",
    group = "Food",
    isIncome = false,
    selected = PREVIEW_MONTH,
    current = PREVIEW_MONTH,
    history = PREVIEW_HISTORY,
  )

private class BudgetCategoryStateProvider :
  ColoredParameterProvider<BudgetCategoryState>(
    PREVIEW_CATEGORY,
    PREVIEW_CATEGORY.copy(type = Tracking, selected = PREVIEW_EARLIER_MONTH),
    PREVIEW_CATEGORY.copy(history = persistentListOf(PREVIEW_HISTORY.last())),
    Failed,
  )

@PortraitPreview
@Composable
private fun PreviewBudgetCategory(
  @PreviewParameter(BudgetCategoryStateProvider::class) params: ColoredParams<BudgetCategoryState>,
) =
  PreviewWithColoredParams(params) {
    BudgetCategoryScaffold(state = this, onBack = {}, onSelect = {}, onViewTransactions = {})
  }
