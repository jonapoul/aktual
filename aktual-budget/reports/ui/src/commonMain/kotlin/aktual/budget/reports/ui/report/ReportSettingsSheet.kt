package aktual.budget.reports.ui.report

import aktual.budget.reports.components.label
import aktual.budget.reports.vm.BudgetAnalysisReportMeta
import aktual.budget.reports.vm.NetWorthMode
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.SummaryChartType
import aktual.budget.reports.vm.SummaryReportMeta
import aktual.budget.reports.vm.allTimeDivisor
import aktual.budget.reports.vm.summaryType
import aktual.budget.reports.vm.withAllTimeDivisor
import aktual.budget.reports.vm.withSummaryType
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf
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
    else -> null
  }

internal fun ReportMeta.hasSettings(): Boolean = settingsSheet() != null

// Only touches its own type of report, in case the draft changes type under it
private inline fun <reified T : ReportMeta> (((ReportMeta) -> ReportMeta) -> Unit).typed():
  ((T) -> T) -> Unit = { transform -> this { meta -> if (meta is T) transform(meta) else meta } }

// The time frame picker replaces the settings while open, then comes back to them
@Composable
private fun NetWorthSettings(
  meta: NetWorthReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((NetWorthReportMeta) -> NetWorthReportMeta) -> Unit,
  onDismiss: () -> Unit,
) {
  var pickTimeFrame by rememberSaveable { mutableStateOf(false) }

  if (pickTimeFrame && editorData != null) {
    TimeFrameSheet(
      timeFrame = meta.timeFrame,
      data = editorData,
      onChange = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
      onDismiss = { pickTimeFrame = false },
    )
    return
  }

  QuerySheet(title = Strings.reportsNetWorthSettings, onDismiss = onDismiss) {
    if (editorData != null) {
      Column {
        SectionLabel(Strings.reportsFormulaQueryTimeFrame)
        TimeFrameRow(timeFrame = meta.timeFrame, onClick = { pickTimeFrame = true })
      }
    }

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
  }
}

private val NET_WORTH_MODES = persistentListOf<NetWorthMode>(Trend, Stacked)

@Composable
private fun SummarySettings(
  meta: SummaryReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((SummaryReportMeta) -> SummaryReportMeta) -> Unit,
  onDismiss: () -> Unit,
) {
  var picker by rememberSaveable { mutableStateOf<SummaryPicker?>(null) }

  when (picker) {
    Dates -> {
      if (editorData != null) {
        TimeFrameSheet(
          timeFrame = meta.timeFrame,
          data = editorData,
          onChange = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
          onDismiss = { picker = null },
        )
        return
      }
    }
    ShowAs -> {
      OptionSheet(
        title = Strings.reportsSummaryType,
        options = SUMMARY_TYPES,
        selected = meta.summaryType,
        isEnabled = { true },
        label = { it.label() },
        onSelect = { type -> onEdit { it.withSummaryType(type) } },
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
        TimeFrameRow(timeFrame = meta.timeFrame, onClick = { picker = Dates })
      }
    }

    Section(label = Strings.reportsCustomEditorDisplay) {
      SettingValueRow(Strings.reportsSummaryType, meta.summaryType.label()) {
        picker = ShowAs
      }
      if (meta.summaryType == Percentage) {
        Divider()
        SwitchRow(Strings.reportsSummaryAllTimeDivisor, meta.allTimeDivisor) { allTime ->
          onEdit { it.withAllTimeDivisor(allTime) }
        }
      }
    }
  }
}

private enum class SummaryPicker {
  Dates,
  ShowAs,
}

private val SUMMARY_TYPES = SummaryChartType.entries.toImmutableList()
