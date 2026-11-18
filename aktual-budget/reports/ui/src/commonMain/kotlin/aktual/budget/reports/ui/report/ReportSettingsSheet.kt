package aktual.budget.reports.ui.report

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.reports.components.label
import aktual.budget.reports.vm.AgeOfMoneyReportMeta
import aktual.budget.reports.vm.BalanceForecastReportMeta
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.CalendarReportMeta
import aktual.budget.reports.vm.CashFlowReportMeta
import aktual.budget.reports.vm.CrossoverReportMeta
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.SankeyReportMeta
import aktual.budget.reports.vm.SummaryChartType
import aktual.budget.reports.vm.SummaryReportMeta
import aktual.budget.reports.vm.allTimeDivisor
import aktual.budget.reports.vm.divisorConditions
import aktual.budget.reports.vm.summaryType
import aktual.budget.reports.vm.withAllTimeDivisor
import aktual.budget.reports.vm.withDivisorConditions
import aktual.budget.reports.vm.withSummaryType
import aktual.budget.rules.components.ConditionSheetAt
import aktual.budget.rules.components.ConditionsList
import aktual.budget.rules.components.Divider
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.toImmutableList

internal typealias SettingsSheet =
  @Composable
  (
    editorData: QueryEditorData?,
    onEdit: ((ReportMeta) -> ReportMeta) -> Unit,
    onDismiss: () -> Unit,
  ) -> Unit

// The sheet edits the report's draft, which the chart behind previews until it's saved. Adding a
// report type here gives it the settings button and sheet
@Suppress("ElseCaseInsteadOfExhaustiveWhen")
internal fun ReportMeta.settingsSheet(): SettingsSheet? =
  when (this) {
    is NetWorthReportMeta -> { data, onEdit, onDismiss ->
      NetWorthSettings(this, data, onEdit.typed(), onDismiss)
    }
    is SummaryReportMeta -> { data, onEdit, onDismiss ->
      SummarySettings(this, data, onEdit.typed(), onDismiss)
    }
    is BudgetAnalysisReportMeta -> { data, onEdit, onDismiss ->
      BudgetAnalysisSettings(this, data, onEdit.typed(), onDismiss)
    }
    is CashFlowReportMeta -> { data, onEdit, onDismiss ->
      CashFlowSettings(this, data, onEdit.typed(), onDismiss)
    }
    is CalendarReportMeta -> { data, onEdit, onDismiss ->
      CalendarSettings(this, data, onEdit.typed(), onDismiss)
    }
    is AgeOfMoneyReportMeta -> { data, onEdit, onDismiss ->
      AgeOfMoneySettings(this, data, onEdit.typed(), onDismiss)
    }
    is CrossoverReportMeta -> { data, onEdit, onDismiss ->
      CrossoverSettings(this, data, onEdit.typed(), onDismiss)
    }
    is SankeyReportMeta -> { data, onEdit, onDismiss ->
      SankeySettings(this, data, onEdit.typed(), onDismiss)
    }
    is BalanceForecastReportMeta -> { data, onEdit, onDismiss ->
      BalanceForecastSettings(this, data, onEdit.typed(), onDismiss)
    }
    else -> null
  }

internal fun ReportMeta.hasSettings(): Boolean = settingsSheet() != null

// Only touches its own type of report, in case the draft changes type under it
private inline fun <reified T : ReportMeta> (((ReportMeta) -> ReportMeta) -> Unit).typed():
  ((T) -> T) -> Unit = { transform -> this { meta -> if (meta is T) transform(meta) else meta } }

@Composable
private fun SummarySettings(
  meta: SummaryReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((SummaryReportMeta) -> SummaryReportMeta) -> Unit,
  onDismiss: () -> Unit,
) {
  var showTypes by rememberSaveable { mutableStateOf(false) }
  var divisorIndex by rememberSaveable { mutableStateOf<Int?>(null) }
  val isPercentage = meta.summaryType == Percentage
  val divisor = meta.divisorConditions
  val divisorConditions = divisor.conditions.toImmutableList()
  val onDivisor = { conditions: List<Condition>, op: ConditionOp ->
    onEdit { it.withDivisorConditions(conditions, op) }
  }

  if (showTypes) {
    OptionSheet(
      title = Strings.reportsSummaryType,
      options = SUMMARY_TYPES,
      selected = meta.summaryType,
      isEnabled = { true },
      label = { it.label() },
      onSelect = { type -> onEdit { it.withSummaryType(type) } },
      onDismiss = { showTypes = false },
    )
    return
  }

  // Falls back to the settings if the condition is gone, e.g. removed by a sync
  val index = divisorIndex?.takeIf { isPercentage && it in divisorConditions.indices }
  if (editorData != null && index != null) {
    ConditionSheetAt(
      index = index,
      conditions = divisorConditions,
      conditionsOp = divisor.op,
      data = editorData,
      onChange = onDivisor,
      onDismiss = { divisorIndex = null },
    )
    return
  }

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
    footer = {
      if (editorData != null && isPercentage) {
        ConditionsList(
          conditions = divisorConditions,
          conditionsOp = divisor.op,
          names = editorData.names,
          onChange = onDivisor,
          onOpen = { divisorIndex = it },
          matchLabel = Strings.reportsSummaryDivisorMatch,
          conditionsLabel = Strings.reportsSummaryDivisorConditions,
        )
      }
    },
  ) {
    Section(label = Strings.reportsCustomEditorDisplay) {
      SettingValueRow(Strings.reportsSummaryType, meta.summaryType.label()) { showTypes = true }
      if (isPercentage) {
        Divider()
        SwitchRow(Strings.reportsSummaryAllTimeDivisor, meta.allTimeDivisor) { allTime ->
          onEdit { it.withAllTimeDivisor(allTime) }
        }
      }
    }
  }
}

private val SUMMARY_TYPES = SummaryChartType.entries.toImmutableList()
