package aktual.crashes.ui

import aktual.about.components.ExportTargetRow
import aktual.about.data.LogExportTarget
import aktual.core.icons.AktualIcons
import aktual.core.icons.Copy
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Save
import aktual.core.icons.material.Share
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParameters
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun CrashExportSheet(
  canShare: Boolean,
  onExport: (includeLogs: Boolean, target: LogExportTarget) -> Unit,
  onDismiss: () -> Unit,
) {
  AktualModalBottomSheet(onDismissRequest = onDismiss) {
    CrashExportSheetContent(
      canShare = canShare,
      onExport = { includeLogs, target ->
        onExport(includeLogs, target)
        onDismiss()
      },
    )
  }
}

@Composable
private fun CrashExportSheetContent(
  canShare: Boolean,
  onExport: (includeLogs: Boolean, target: LogExportTarget) -> Unit,
  modifier: Modifier = Modifier,
) {
  var includeLogs by remember { mutableStateOf(true) }

  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = Dimens.Huge).padding(bottom = 24.dp),
    verticalArrangement = spacedBy(Dimens.Huge),
  ) {
    Text(text = Strings.crashesExportTitle, style = typography.headlineSmall)

    Column(verticalArrangement = spacedBy(Dimens.Large)) {
      Text(
        text = Strings.crashesExportInclude,
        style = typography.labelMedium,
        color = colors.pageTextLight,
      )
      FlowRow(horizontalArrangement = spacedBy(6.dp), verticalArrangement = spacedBy(6.dp)) {
        IncludeChip(Strings.crashesExportWithLogs, isSelected = includeLogs) { includeLogs = true }
        IncludeChip(Strings.crashesExportReportOnly, isSelected = !includeLogs) {
          includeLogs = false
        }
      }
    }

    Column(verticalArrangement = spacedBy(Dimens.Medium)) {
      if (canShare) {
        ExportTargetRow(
          icon = MaterialIcons.Share,
          text = Strings.logsExportShare,
          onClick = { onExport(includeLogs, LogExportTarget.Share) },
        )
      }
      ExportTargetRow(
        icon = MaterialIcons.Save,
        text = Strings.logsExportSave,
        onClick = { onExport(includeLogs, LogExportTarget.Save) },
      )
      ExportTargetRow(
        icon = AktualIcons.Copy,
        text = Strings.logsExportCopy,
        onClick = { onExport(includeLogs, LogExportTarget.Copy) },
      )
    }
  }
}

@Composable
private fun IncludeChip(text: String, isSelected: Boolean, onClick: () -> Unit) =
  Chip(
    modifier = Modifier.semantics { selected = isSelected },
    text = text,
    colors = ChipDefaults.selectableColors(isSelected),
    onClick = onClick,
  )

@Preview
@Composable
private fun PreviewCrashExportSheet(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { CrashExportSheetContent(canShare = true, onExport = { _, _ -> }) }
