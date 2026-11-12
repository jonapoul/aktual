package aktual.budget.reports.ui.report

import aktual.budget.model.AccountId
import aktual.budget.reports.vm.BalanceForecastReportMeta
import aktual.budget.reports.vm.ForecastGranularity
import aktual.budget.reports.vm.ForecastSource
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.YAxisBase
import aktual.budget.reports.vm.forecastTimeFrame
import aktual.budget.reports.vm.startsTodayMonths
import aktual.budget.reports.vm.withSource
import aktual.budget.reports.vm.withStartToday
import aktual.budget.rules.components.QuerySheetPage
import aktual.budget.rules.components.SectionLabel
import aktual.budget.rules.components.label
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

// packages/desktop-client/src/components/reports/reports/BalanceForecast.tsx
@Composable
internal fun BalanceForecastSettings(
  meta: BalanceForecastReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((BalanceForecastReportMeta) -> BalanceForecastReportMeta) -> Unit,
  onDismiss: () -> Unit,
) {
  var pickAccounts by rememberSaveable { mutableStateOf(false) }

  if (pickAccounts && editorData != null) {
    AccountSheet(
      title = Strings.reportsBalanceForecastAccounts,
      accounts = editorData.allAccounts,
      selected = meta.selectedAccounts(editorData),
      // Picking every account goes back to unset, which also counts schedules with no account
      onChange = { selected ->
        val all = selected.size == editorData.allAccounts.size
        onEdit { it.copy(accounts = if (all) null else selected.toList()) }
      },
      onDismiss = { pickAccounts = false },
    )
    return
  }

  val monthsFromToday = meta.startsTodayMonths(meta.isTracking(editorData))

  FilteredReportSettings(
    timeFrame = meta.forecastTimeFrame(),
    conditions = meta.conditions.orEmpty().toImmutableList(),
    conditionsOp = meta.conditionsOp ?: And,
    editorData = editorData,
    onTimeFrame = { timeFrame ->
      onEdit { it.copy(timeFrame = timeFrame, monthsFromToday = null) }
    },
    onConditions = { conditions, op ->
      onEdit { it.copy(conditions = conditions, conditionsOp = op) }
    },
    onDismiss = onDismiss,
    showTimeFrame = monthsFromToday == null,
  ) {
    BalanceForecastDisplay(meta, editorData, onEdit, onPickAccounts = { pickAccounts = true })
  }
}

// The forecast's own settings, between the time frame and the filters
@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.BalanceForecastDisplay(
  meta: BalanceForecastReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((BalanceForecastReportMeta) -> BalanceForecastReportMeta) -> Unit,
  onPickAccounts: () -> Unit,
) {
  val isTracking = meta.isTracking(editorData)
  val monthsFromToday = meta.startsTodayMonths(isTracking)

  if (editorData != null && !isTracking) {
    Section(label = Strings.reportsFormulaQueryTimeFrame) {
      SwitchRow(Strings.reportsBalanceForecastStartToday, monthsFromToday != null) { enabled ->
        onEdit { it.withStartToday(enabled, editorData.currentMonth) }
      }
    }

    if (monthsFromToday != null) {
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = monthsFromToday,
        options = (FROM_TODAY_MONTHS + monthsFromToday).distinct().sorted().toImmutableList(),
        onSelect = { months -> onEdit { it.copy(monthsFromToday = months) } },
        string = { Plurals.reportsBalanceForecastNextMonths(it, it) },
      )

      Text(
        modifier = Modifier.padding(horizontal = 4.dp),
        text = Strings.reportsBalanceForecastStartTodayNote,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }
  }

  if (editorData?.isTrackingBudget == true) {
    Column {
      SectionLabel(Strings.reportsBalanceForecastSource)
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = if (isTracking) TrackingBudget else Schedules,
        options = SOURCES,
        onSelect = { source -> onEdit { it.withSource(source, editorData.currentMonth) } },
        string = { it.label() },
      )
    }
  }

  if (!isTracking) {
    Column {
      SectionLabel(Strings.reportsBalanceForecastGranularity)
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = meta.granularity?.takeIf { it != Unknown } ?: Monthly,
        options = GRANULARITIES,
        onSelect = { granularity -> onEdit { it.copy(granularity = granularity) } },
        string = { it.label() },
      )
    }
  }

  Column {
    SectionLabel(Strings.reportsBalanceForecastYAxis)
    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth(),
      selected = if (meta.yAxisBase == ZeroBased) ZeroBased else Auto,
      options = Y_AXIS_BASES,
      onSelect = { base -> onEdit { it.copy(yAxisBase = base) } },
      string = { it.label() },
    )
  }

  if (editorData != null && !isTracking) {
    Section(label = Strings.reportsCustomEditorDisplay) {
      SettingValueRow(
        Strings.reportsBalanceForecastAccounts,
        countLabel(meta.selectedAccounts(editorData).size, editorData.allAccounts.size),
        onClick = onPickAccounts,
      )
    }
  }
}

// The tracking budget forecast doesn't use schedules, so accounts, daily points and starting today
// don't apply to it
private fun BalanceForecastReportMeta.isTracking(data: QueryEditorData?): Boolean =
  data?.isTrackingBudget == true && source == TrackingBudget

private fun BalanceForecastReportMeta.selectedAccounts(
  data: QueryEditorData,
): ImmutableSet<AccountId> =
  (accounts?.toSet() ?: data.allAccounts.map { AccountId(it.id) }.toSet()).toImmutableSet()

@Composable
private fun ForecastSource.label(): String =
  when (this) {
    TrackingBudget -> Strings.reportsBalanceForecastSourceTracking
    Schedules,
    Unknown -> Strings.reportsBalanceForecastSourceSchedules
  }

@Composable
private fun ForecastGranularity.label(): String =
  when (this) {
    Daily -> Strings.reportsBalanceForecastDaily
    Monthly,
    Unknown -> Strings.reportsBalanceForecastMonthly
  }

@Composable
private fun YAxisBase.label(): String =
  when (this) {
    ZeroBased -> Strings.reportsBalanceForecastYAxisZero
    Auto,
    Unknown -> Strings.reportsBalanceForecastYAxisAuto
  }

private val SOURCES = persistentListOf<ForecastSource>(Schedules, TrackingBudget)

private val GRANULARITIES = persistentListOf<ForecastGranularity>(Monthly, Daily)

private val Y_AXIS_BASES = persistentListOf<YAxisBase>(Auto, ZeroBased)

// Upstream's "from today" presets
private val FROM_TODAY_MONTHS = listOf(3, 6, 12)

@Preview
@Composable
private fun PreviewBalanceForecastSettings(
  @PreviewParameter(BalanceForecastMetaProvider::class)
  params: ColorParams<BalanceForecastReportMeta>,
) =
  PreviewWithColorParams(params) {
    QuerySheetPage(title = Strings.reportsChartSettings, close = {}) {
      BalanceForecastDisplay(
        meta = this@PreviewWithColorParams,
        editorData = PREVIEW_QUERY_DATA.copy(isTrackingBudget = true),
        onEdit = {},
        onPickAccounts = {},
      )
    }
  }

private class BalanceForecastMetaProvider :
  ColorParamsProvider<BalanceForecastReportMeta>(
    BalanceForecastReportMeta(),
    BalanceForecastReportMeta(granularity = Daily, yAxisBase = ZeroBased, monthsFromToday = 6),
    BalanceForecastReportMeta(source = TrackingBudget),
  )
