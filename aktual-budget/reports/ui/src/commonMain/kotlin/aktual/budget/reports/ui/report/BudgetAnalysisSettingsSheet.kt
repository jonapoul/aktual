package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.BUDGET_ANALYSIS_FIELDS
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.GraphType
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.settings
import aktual.core.l10n.Strings
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// Pickers and conditions replace the settings while open, then come back to them
@Composable
internal fun BudgetAnalysisSettings(
  meta: BudgetAnalysisReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((BudgetAnalysisReportMeta) -> BudgetAnalysisReportMeta) -> Unit,
  onDismiss: () -> Unit,
) {
  var picker by rememberSaveable { mutableStateOf<BudgetAnalysisPicker?>(null) }
  var conditionIndex by rememberSaveable { mutableStateOf<Int?>(null) }

  // The draft always has a time frame, since upstream's defaults are filled in
  val settings = meta.timeFrame?.let(meta::settings) ?: return

  // Falls back to the settings if the condition is gone, e.g. removed by a sync
  val index = conditionIndex
  if (index != null && editorData != null && index in settings.conditions.indices) {
    ConditionSheetAt(
      index = index,
      conditions = settings.conditions,
      conditionsOp = settings.conditionsOp,
      data = editorData,
      fields = BUDGET_ANALYSIS_FIELDS,
      onChange = { conditions, op ->
        onEdit { it.copy(conditions = conditions, conditionsOp = op) }
      },
      onDismiss = { conditionIndex = null },
    )
    return
  }

  when (picker) {
    Dates -> {
      if (editorData != null) {
        TimeFrameSheet(
          timeFrame = settings.timeFrame,
          data = editorData,
          onChange = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
          onDismiss = { picker = null },
        )
        return
      }
    }
    Graph -> {
      OptionSheet(
        title = Strings.reportsCustomEditorGraph,
        options = GRAPH_TYPES,
        selected = settings.graphType,
        isEnabled = { true },
        label = { it.label() },
        onSelect = { graph -> onEdit { it.copy(graphType = graph) } },
        onDismiss = { picker = null },
      )
      return
    }
    null -> {}
  }

  QuerySheet(title = Strings.reportsChartSettings, onDismiss = onDismiss) {
    if (editorData != null) {
      Column {
        SectionLabel(Strings.reportsFormulaQueryTimeFrame)
        TimeFrameRow(timeFrame = settings.timeFrame, onClick = { picker = Dates })
      }
    }

    Section(label = Strings.reportsCustomEditorDisplay) {
      SettingValueRow(Strings.reportsCustomEditorGraph, settings.graphType.label()) {
        picker = Graph
      }
      Divider()
      SwitchRow(Strings.reportsBudgetAnalysisShowBalance, settings.showBalance) { show ->
        onEdit { it.copy(showBalance = show) }
      }
      Divider()
      SwitchRow(Strings.reportsBudgetAnalysisBalanceOnly, settings.balanceOnly) { only ->
        onEdit { it.copy(balanceOnly = only) }
      }
      Divider()
      SwitchRow(Strings.reportsCustomEditorShowHidden, settings.showHiddenCategories) { show ->
        onEdit { it.copy(showHiddenCategories = show) }
      }
    }

    if (editorData != null) {
      Column {
        SectionLabel(Strings.reportsCustomEditorFilters)
        ConditionsList(
          conditions = settings.conditions,
          conditionsOp = settings.conditionsOp,
          names = editorData.names,
          fields = BUDGET_ANALYSIS_FIELDS,
          onChange = { conditions, op ->
            onEdit { it.copy(conditions = conditions.toImmutableList(), conditionsOp = op) }
          },
          onOpen = { conditionIndex = it },
        )
      }
    }
  }
}

private enum class BudgetAnalysisPicker {
  Dates,
  Graph,
}

@Composable
private fun GraphType.label(): String =
  when (this) {
    Bar -> Strings.reportsCustomEditorGraphBar
    Line -> Strings.reportsCustomEditorGraphLine
    Unknown -> Strings.reportsCustomEditorUnknown
  }

private val GRAPH_TYPES = persistentListOf<GraphType>(Bar, Line)
