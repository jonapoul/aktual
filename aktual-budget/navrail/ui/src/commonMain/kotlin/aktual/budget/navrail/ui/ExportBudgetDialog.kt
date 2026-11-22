package aktual.budget.navrail.ui

import aktual.budget.navrail.vm.ExportBudgetState
import aktual.budget.navrail.vm.ExportBudgetTarget
import aktual.budget.navrail.vm.ExportBudgetViewModel
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Save
import aktual.core.icons.material.Share
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualProgressIndicator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.DialogTextButton
import aktual.core.ui.Dimens
import aktual.core.ui.ExportFile
import aktual.core.ui.ExportTargetRow
import aktual.core.ui.FileExportHandler
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun ExportBudgetDialog(
  viewModel: ExportBudgetViewModel,
  canShare: Boolean,
  onDismiss: () -> Unit,
) {
  val state by viewModel.state.collectAsState()
  val close = {
    viewModel.reset()
    onDismiss()
  }
  AktualAlertDialog(onDismissRequest = close) {
    ExportBudgetDialogContent(
      state = state,
      isEncrypted = viewModel.isEncrypted,
      canShare = canShare,
      onExport = viewModel::export,
      onDismiss = close,
    )
  }
}

// Hands finished exports to the handler, which lives outside the dialog so Android's save
// launcher survives the dialog closing
@Composable
internal fun CollectBudgetExports(
  viewModel: ExportBudgetViewModel,
  handler: FileExportHandler,
  onReady: () -> Unit,
) {
  val currentHandler by rememberUpdatedState(handler)
  val currentOnReady by rememberUpdatedState(onReady)
  LaunchedEffect(viewModel) {
    viewModel.exports.collect { export ->
      currentOnReady()
      val file = ExportFile(export.fileName, export.write)
      when (export.target) {
        ExportBudgetTarget.Share -> currentHandler.share(file)
        ExportBudgetTarget.Save -> currentHandler.save(file)
      }
    }
  }
}

@Composable
private fun ExportBudgetDialogContent(
  state: ExportBudgetState,
  isEncrypted: Boolean,
  canShare: Boolean,
  onExport: (ExportBudgetTarget) -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialogContent(
    title = Strings.budgetNavMenuExportBudget,
    buttons = { DialogTextButton(text = Strings.budgetExportClose, onClick = onDismiss) },
    content = {
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = spacedBy(Dimens.Large)) {
        Text(
          text = Strings.budgetExportDescription,
          style = typography.bodyMedium,
          color = colors.pageText,
        )

        if (isEncrypted) {
          Text(
            text = Strings.budgetExportEncrypted,
            style = typography.bodyMedium,
            color = colors.warningText,
          )
        }

        if (state is Failed) {
          Text(
            text = Strings.budgetExportFailed(state.message),
            style = typography.bodyMedium,
            color = colors.errorText,
          )
        }

        if (state is Exporting) {
          AktualProgressIndicator(
            modifier = Modifier.align(CenterHorizontally),
            size = 32.dp,
          )
        } else {
          Column(verticalArrangement = spacedBy(Dimens.Medium)) {
            if (canShare) {
              ExportTargetRow(
                icon = MaterialIcons.Share,
                text = Strings.budgetExportShare,
                onClick = { onExport(ExportBudgetTarget.Share) },
              )
            }
            ExportTargetRow(
              icon = MaterialIcons.Save,
              text = Strings.budgetExportSave,
              onClick = { onExport(ExportBudgetTarget.Save) },
            )
          }
        }
      }
    },
  )

@Preview
@Composable
private fun PreviewExportBudgetDialog(
  @PreviewParameter(ExportBudgetDialogProvider::class) params: ColorParams<ExportBudgetState>,
) =
  PreviewWithColorParams(params) {
    ExportBudgetDialogContent(
      state = params.data,
      isEncrypted = true,
      canShare = true,
      onExport = {},
      onDismiss = {},
    )
  }

private class ExportBudgetDialogProvider :
  ColorParamsProvider<ExportBudgetState>(
    ExportBudgetState.Idle,
    ExportBudgetState.Exporting,
    ExportBudgetState.Failed("Disk full"),
  )
