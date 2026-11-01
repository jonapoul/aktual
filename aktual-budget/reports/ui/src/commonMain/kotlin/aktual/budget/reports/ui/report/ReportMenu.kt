package aktual.budget.reports.ui.report

import aktual.budget.reports.components.ChartTags
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.dashboard.NameDialog
import aktual.core.icons.AktualIcons
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.InformationOutline
import aktual.core.icons.Pencil1
import aktual.core.l10n.Strings
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.BareIconButton
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

@Composable
internal fun ReportMenu(
  name: String,
  onRename: ((String) -> Unit)?,
  modifier: Modifier = Modifier,
  takenNames: ImmutableSet<String> = persistentSetOf(),
  onShowInfo: (() -> Unit)? = null,
) {
  var expanded by remember { mutableStateOf(false) }
  var showRenameDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    BareIconButton(
      modifier = Modifier.testTag(Tags.ReportMenu),
      imageVector = AktualIcons.DotsHorizontalTriple,
      contentDescription = Strings.reportsReportMenu,
      onClick = { expanded = true },
    )

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      if (onShowInfo != null) {
        AktualDropdownMenuItem(
          modifier = Modifier.testTag(ChartTags.ChartInfo),
          text = Strings.reportsChartInfo,
          leadingIcon = AktualIcons.InformationOutline,
          onClick = {
            expanded = false
            onShowInfo()
          },
        )
      }
      if (onRename != null) {
        AktualDropdownMenuItem(
          text = Strings.reportsDashboardRename,
          leadingIcon = AktualIcons.Pencil1,
          onClick = {
            expanded = false
            showRenameDialog = true
          },
        )
      }
    }
  }

  if (showRenameDialog && onRename != null) {
    val nameTaken = Strings.reportsCustomEditorNameTaken
    NameDialog(
      title = Strings.reportsDashboardRenameReport,
      placeholder = Strings.reportsDashboardReportName,
      confirmText = Strings.reportsDashboardNameSave,
      initialName = name,
      error = { if (it in takenNames) nameTaken else null },
      onConfirm = { newName ->
        showRenameDialog = false
        onRename(newName)
      },
      onDismiss = { showRenameDialog = false },
    )
  }
}
