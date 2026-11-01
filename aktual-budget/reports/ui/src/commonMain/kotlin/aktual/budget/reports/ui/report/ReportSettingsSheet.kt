package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.NetWorthMode
import aktual.budget.reports.vm.NetWorthReportMeta
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.ReportMeta
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

internal fun ReportMeta.hasSettings(): Boolean = this is NetWorthReportMeta

// Edits the report's draft, which the chart behind previews until it's saved
@Composable
internal fun ReportSettingsSheet(
  draft: ReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((ReportMeta) -> ReportMeta) -> Unit,
  onDismiss: () -> Unit,
) {
  if (draft is NetWorthReportMeta) {
    NetWorthSettings(
      meta = draft,
      editorData = editorData,
      onEdit = { transform ->
        onEdit { meta -> if (meta is NetWorthReportMeta) transform(meta) else meta }
      },
      onDismiss = onDismiss,
    )
  }
}

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
