package aktual.about.ui.logs

import aktual.about.vm.logs.LogExportScope
import aktual.about.vm.logs.LogExportScope.Day
import aktual.about.vm.logs.LogExportTarget
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
import aktual.core.ui.ColoredParameters
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

@Composable
internal fun LogExportSheet(
  scopes: ImmutableList<LogExportScope>,
  today: LocalDate?,
  canShare: Boolean,
  onExport: (LogExportScope, LogExportTarget) -> Unit,
  onDismiss: () -> Unit,
) {
  AktualModalBottomSheet(onDismissRequest = onDismiss) {
    LogExportSheetContent(
      scopes,
      today,
      canShare,
      onExport = { scope, target ->
        onExport(scope, target)
        onDismiss()
      },
    )
  }
}

@Composable
private fun LogExportSheetContent(
  scopes: ImmutableList<LogExportScope>,
  today: LocalDate?,
  canShare: Boolean,
  onExport: (LogExportScope, LogExportTarget) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selected by remember(scopes) { mutableStateOf(scopes.first()) }

  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = Dimens.Huge).padding(bottom = 24.dp),
    verticalArrangement = spacedBy(Dimens.Huge),
  ) {
    Text(text = Strings.logsExportTitle, style = typography.headlineSmall)

    Column(verticalArrangement = spacedBy(Dimens.Large)) {
      Text(
        text = Strings.logsExportInclude,
        style = typography.labelMedium,
        color = colors.pageTextLight,
      )
      FlowRow(
        horizontalArrangement = spacedBy(6.dp),
        verticalArrangement = spacedBy(6.dp),
      ) {
        for (scope in scopes) {
          val isSelected = scope == selected
          Chip(
            modifier = Modifier.semantics { this.selected = isSelected },
            text = scope.label(today),
            colors = ChipDefaults.selectableColors(isSelected),
            onClick = { selected = scope },
          )
        }
      }
    }

    Column(verticalArrangement = spacedBy(Dimens.Medium)) {
      if (canShare) {
        ExportTargetRow(MaterialIcons.Share, Strings.logsExportShare) {
          onExport(selected, LogExportTarget.Share)
        }
      }
      ExportTargetRow(MaterialIcons.Save, Strings.logsExportSave) {
        onExport(selected, LogExportTarget.Save)
      }
      ExportTargetRow(AktualIcons.Copy, Strings.logsExportCopy) {
        onExport(selected, LogExportTarget.Copy)
      }
    }
  }
}

@Composable
private fun ExportTargetRow(icon: ImageVector, text: String, onClick: () -> Unit) =
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .height(52.dp)
        .clip(RowShape)
        .background(colors.tableBackground)
        .clickable(onClick = onClick)
        .padding(horizontal = Dimens.VeryLarge),
    verticalAlignment = CenterVertically,
    horizontalArrangement = spacedBy(14.dp),
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = colors.pageTextLight)
    Text(text = text, style = typography.bodyLarge, color = colors.tableText)
  }

@Composable
private fun LogExportScope.label(today: LocalDate?): String =
  when (this) {
    Shown -> Strings.logsExportShown
    is Day -> if (date == today) Strings.logsExportToday else Strings.logsExportDay
    LastWeek -> Strings.logsExportLastWeek
    All -> Strings.logsExportAll
  }

@Preview
@Composable
private fun PreviewLogExportSheet(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    LogExportSheetContent(
      scopes =
        persistentListOf<LogExportScope>(
          Shown,
          Day(LocalDate(2026, 10, 18)),
          LastWeek,
          All,
        ),
      today = null,
      canShare = true,
      onExport = { _, _ -> },
    )
  }
