package aktual.budget.reports.ui

import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.ui.charts.JUL_2025
import aktual.budget.reports.ui.charts.PER_TRANSACTION_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CASH_FLOW_DATA
import aktual.budget.reports.ui.charts.PREVIEW_CUSTOM_DATA
import aktual.budget.reports.ui.charts.PREVIEW_NET_WORTH_DATA
import aktual.budget.reports.ui.charts.PREVIEW_SHORT_TEXT_DATA
import aktual.budget.reports.ui.charts.ReportChart
import aktual.budget.reports.ui.charts.THREE_MONTHS
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.ChooseReportTypeViewModel
import aktual.core.model.ColorSchemeType
import aktual.core.model.LoginToken
import aktual.core.ui.AktualTypography
import aktual.core.ui.BackHandler
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColorSchemeParameters
import aktual.core.ui.LocalTheme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.WavyBackground
import aktual.core.ui.WithHazeState
import aktual.core.ui.aktualHaze
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.l10n.Strings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChooseReportTypeScreen(
  nav: ChooseReportTypeNavigator,
  budgetId: BudgetId,
  token: LoginToken,
  viewModel: ChooseReportTypeViewModel = metroViewModel(budgetId),
) {
  BackHandler { nav.back() }

  LaunchedEffect(Unit) {
    viewModel.shouldNavigateEvent.collectLatest { event ->
      nav.toReport(token, budgetId, widgetId = event.id)
    }
  }

  ChooseReportTypeScaffold(
    onAction = { action ->
      when (action) {
        is ChooseReportTypeAction.Create -> viewModel.createReport(action.type)
      }
    },
  )
}

@Composable
internal fun ChooseReportTypeScaffold(
  onAction: ChooseReportTypeActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Scaffold(
  modifier = modifier,
  topBar = {
    TopAppBar(
      colors = theme.transparentTopAppBarColors(),
      title = { Text(Strings.reportsChooseTypeTitle) },
    )
  },
) { innerPadding ->
  Box {
    val hazeState = remember { HazeState() }

    WavyBackground(
      modifier = Modifier.hazeSource(hazeState),
    )

    WithHazeState(hazeState) {
      Content(
        modifier = Modifier.padding(innerPadding),
        onAction = onAction,
        theme = theme,
      )
    }
  }
}

@Composable
private fun Content(
  onAction: ChooseReportTypeActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val lazyListState = rememberLazyListState()
  LazyColumn(
    modifier = modifier
      .fillMaxWidth()
      .scrollbar(lazyListState),
    state = lazyListState,
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(8.dp),
  ) {
    items(WIDGET_TYPES) { type ->
      WidgetType(
        modifier = Modifier.fillMaxWidth(),
        type = type,
        onAction = onAction,
        theme = theme,
      )
    }

    item {
      BottomStatusBarSpacing()
      BottomNavBarSpacing()
    }
  }
}

@Composable
private fun WidgetType(
  type: WidgetType,
  onAction: ChooseReportTypeActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Column(
  modifier = modifier
    .clip(CardShape)
    .aktualHaze()
    .clickable { onAction(ChooseReportTypeAction.Create(type)) }
    .padding(8.dp),
) {
  Text(
    modifier = Modifier.padding(8.dp),
    text = type.string(),
    color = theme.pageText,
    style = AktualTypography.titleLarge,
  )

  ReportChart(
    modifier = Modifier
      .fillMaxWidth()
      .height(REPORT_HEIGHT),
    data = type.sampleData(),
    compact = true,
    includeHeader = false,
    theme = theme,
    onAction = {}, // no-op
  )
}

@Composable
private fun WidgetType.string() = when (this) {
  WidgetType.NetWorth -> Strings.reportsChooseTypeNetWorth
  WidgetType.CashFlow -> Strings.reportsChooseTypeCashFlow
  WidgetType.Spending -> Strings.reportsChooseTypeSpending
  WidgetType.Custom -> Strings.reportsChooseTypeCustom
  WidgetType.Markdown -> Strings.reportsChooseTypeMarkdown
  WidgetType.Summary -> Strings.reportsChooseTypeSummary
  WidgetType.Calendar -> Strings.reportsChooseTypeCalendar
}

@Stable
private fun WidgetType.sampleData(): ChartData = when (this) {
  WidgetType.NetWorth -> PREVIEW_NET_WORTH_DATA
  WidgetType.CashFlow -> PREVIEW_CASH_FLOW_DATA
  WidgetType.Spending -> JUL_2025
  WidgetType.Custom -> PREVIEW_CUSTOM_DATA
  WidgetType.Markdown -> PREVIEW_SHORT_TEXT_DATA
  WidgetType.Summary -> PER_TRANSACTION_DATA
  WidgetType.Calendar -> THREE_MONTHS
}

@Composable
private fun metroViewModel(
  budgetId: BudgetId,
) = assistedMetroViewModel<ChooseReportTypeViewModel, ChooseReportTypeViewModel.Factory>(
  key = budgetId.value,
  createViewModel = { create(budgetId) },
)

@Immutable
internal sealed interface ChooseReportTypeAction {
  data class Create(val type: WidgetType) : ChooseReportTypeAction
}

@Immutable
internal fun interface ChooseReportTypeActionListener {
  operator fun invoke(action: ChooseReportTypeAction)
}

private val WIDGET_TYPES = WidgetType.entries.toImmutableList()
private val REPORT_HEIGHT = 250.dp

@PortraitPreview
@Composable
private fun PreviewLoaded(
  @PreviewParameter(ColorSchemeParameters::class) type: ColorSchemeType,
) = PreviewWithColorScheme(type) {
  ChooseReportTypeScaffold(
    onAction = {},
  )
}
