package aktual.budget.reports.ui.report

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Interval
import aktual.budget.reports.vm.AgeOfMoneyGranularity
import aktual.budget.reports.vm.AgeOfMoneyReportMeta
import aktual.budget.reports.vm.CalendarReportMeta
import aktual.budget.reports.vm.CashFlowReportMeta
import aktual.budget.reports.vm.NetWorthMode
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.TimeFrame
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun NetWorthSettings(
  meta: NetWorthReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((NetWorthReportMeta) -> NetWorthReportMeta) -> Unit,
  onDismiss: () -> Unit,
) =
  FilteredReportSettings(
    title = Strings.reportsNetWorthSettings,
    timeFrame = meta.timeFrame,
    conditions = meta.conditions.orEmpty().toImmutableList(),
    conditionsOp = meta.conditionsOp ?: And,
    editorData = editorData,
    onTimeFrame = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
    onConditions = { conditions, op ->
      onEdit { it.copy(conditions = conditions, conditionsOp = op) }
    },
    onDismiss = onDismiss,
  ) {
    Column {
      SectionLabel(Strings.reportsNetWorthMode)
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = if (meta.mode == Stacked) Stacked else Trend,
        options = NET_WORTH_MODES,
        onSelect = { mode -> onEdit { it.copy(mode = mode) } },
        string = { option ->
          if (option == Stacked) {
            Strings.reportsNetWorthModeStacked
          } else {
            Strings.reportsNetWorthModeTrend
          }
        },
      )
    }

    Column {
      SectionLabel(Strings.reportsCustomEditorInterval)
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = meta.interval?.takeIf { it in NET_WORTH_INTERVALS } ?: Monthly,
        options = NET_WORTH_INTERVALS,
        onSelect = { interval -> onEdit { it.copy(interval = interval) } },
        string = { it.label() },
      )
    }
  }

@Composable
internal fun CashFlowSettings(
  meta: CashFlowReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((CashFlowReportMeta) -> CashFlowReportMeta) -> Unit,
  onDismiss: () -> Unit,
) =
  FilteredReportSettings(
    timeFrame = meta.timeFrame,
    conditions = meta.conditions.orEmpty().toImmutableList(),
    conditionsOp = meta.conditionsOp ?: And,
    editorData = editorData,
    onTimeFrame = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
    onConditions = { conditions, op ->
      onEdit { it.copy(conditions = conditions, conditionsOp = op) }
    },
    onDismiss = onDismiss,
  ) {
    Section(label = Strings.reportsCustomEditorDisplay) {
      SwitchRow(Strings.reportsBudgetAnalysisShowBalance, meta.showBalance != false) { show ->
        onEdit { it.copy(showBalance = show) }
      }
    }
  }

@Composable
internal fun CalendarSettings(
  meta: CalendarReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((CalendarReportMeta) -> CalendarReportMeta) -> Unit,
  onDismiss: () -> Unit,
) =
  FilteredReportSettings(
    timeFrame = meta.timeFrame,
    conditions = meta.conditions.orEmpty().toImmutableList(),
    conditionsOp = meta.conditionsOp ?: And,
    editorData = editorData,
    onTimeFrame = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
    onConditions = { conditions, op ->
      onEdit { it.copy(conditions = conditions, conditionsOp = op) }
    },
    onDismiss = onDismiss,
  )

@Composable
internal fun AgeOfMoneySettings(
  meta: AgeOfMoneyReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((AgeOfMoneyReportMeta) -> AgeOfMoneyReportMeta) -> Unit,
  onDismiss: () -> Unit,
) =
  FilteredReportSettings(
    timeFrame = meta.timeFrame,
    conditions = meta.conditions.orEmpty().toImmutableList(),
    conditionsOp = meta.conditionsOp ?: And,
    editorData = editorData,
    onTimeFrame = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
    onConditions = { conditions, op ->
      onEdit { it.copy(conditions = conditions, conditionsOp = op) }
    },
    onDismiss = onDismiss,
  ) {
    Column {
      SectionLabel(Strings.reportsAgeOfMoneyGranularity)
      AktualSlidingToggleButton(
        modifier = Modifier.fillMaxWidth(),
        selected = meta.granularity?.takeIf { it != Unknown } ?: Monthly,
        options = GRANULARITIES,
        onSelect = { granularity -> onEdit { it.copy(granularity = granularity) } },
        string = { it.label() },
      )
    }
  }

// Settings for a report with a time frame, filters and maybe some display options between them.
// The time frame picker and conditions replace the settings while open, then come back to them
@Composable
internal fun FilteredReportSettings(
  timeFrame: TimeFrame?,
  conditions: ImmutableList<Condition>,
  conditionsOp: ConditionOp,
  editorData: QueryEditorData?,
  onTimeFrame: (TimeFrame) -> Unit,
  onConditions: (List<Condition>, ConditionOp) -> Unit,
  onDismiss: () -> Unit,
  title: String = Strings.reportsChartSettings,
  showTimeFrame: Boolean = true,
  display: (@Composable ColumnScope.() -> Unit)? = null,
) {
  var pickTimeFrame by rememberSaveable { mutableStateOf(false) }
  var conditionIndex by rememberSaveable { mutableStateOf<Int?>(null) }

  if (editorData != null) {
    // Falls back to the settings if the condition is gone, e.g. removed by a sync
    val index = conditionIndex
    if (index != null && index in conditions.indices) {
      ConditionSheetAt(
        index = index,
        conditions = conditions,
        conditionsOp = conditionsOp,
        data = editorData,
        onChange = onConditions,
        onDismiss = { conditionIndex = null },
      )
      return
    }

    if (pickTimeFrame) {
      TimeFrameSheet(
        timeFrame = timeFrame,
        data = editorData,
        onChange = onTimeFrame,
        onDismiss = { pickTimeFrame = false },
      )
      return
    }
  }

  QuerySheet(title = title, onDismiss = onDismiss) {
    if (editorData != null && showTimeFrame) {
      Column {
        SectionLabel(Strings.reportsFormulaQueryTimeFrame)
        TimeFrameRow(timeFrame = timeFrame, onClick = { pickTimeFrame = true })
      }
    }

    display?.invoke(this)

    if (editorData != null) {
      Column {
        SectionLabel(Strings.reportsCustomEditorFilters)
        ConditionsList(
          conditions = conditions,
          conditionsOp = conditionsOp,
          names = editorData.names,
          onChange = onConditions,
          onOpen = { conditionIndex = it },
        )
      }
    }
  }
}

@Composable
private fun AgeOfMoneyGranularity.label(): String =
  when (this) {
    Daily -> Strings.reportsCustomEditorIntervalDaily
    Weekly -> Strings.reportsCustomEditorIntervalWeekly
    Monthly -> Strings.reportsCustomEditorIntervalMonthly
    Unknown -> Strings.reportsCustomEditorUnknown
  }

private val NET_WORTH_MODES = persistentListOf<NetWorthMode>(Trend, Stacked)

// Upstream only offers quarters in custom reports
private val NET_WORTH_INTERVALS = persistentListOf<Interval>(Daily, Weekly, Monthly, Yearly)

private val GRANULARITIES = persistentListOf<AgeOfMoneyGranularity>(Daily, Weekly, Monthly)
