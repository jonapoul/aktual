package aktual.budget.list.ui

import aktual.budget.list.vm.ImportBudgetState
import aktual.budget.list.vm.ImportFailure
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.DialogTextButton
import aktual.core.ui.Dimens
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

// packages/desktop-client/src/components/modals/manager/ImportActualModal.tsx
@Composable
internal fun ImportBudgetDialog(
  state: ImportBudgetState,
  onSelectFile: () -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialog(onDismissRequest = onDismiss) {
    ImportBudgetDialogContent(state = state, onSelectFile = onSelectFile, onDismiss = onDismiss)
  }

@Composable
private fun ImportBudgetDialogContent(
  state: ImportBudgetState,
  onSelectFile: () -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialogContent(
    title = Strings.budgetImportTitle,
    buttons = { DialogTextButton(text = Strings.budgetImportClose, onClick = onDismiss) },
    content = {
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = spacedBy(Dimens.Large)) {
        if (state is Failed) {
          Text(
            text = state.reason.message(),
            style = typography.bodyMedium,
            color = colors.errorText,
          )
        }

        Text(
          text = Strings.budgetImportDescription,
          style = typography.bodyMedium,
          color = colors.pageText,
        )
        Text(
          text = Strings.budgetImportSelectHint,
          style = typography.bodyMedium,
          color = colors.pageText,
        )

        AktualButton(
          modifier = Modifier.align(CenterHorizontally),
          text = Strings.budgetImportSelect,
          kind = Primary,
          isLoading = state is Importing,
          onClick = onSelectFile,
        )
      }
    },
  )

@Composable
private fun ImportFailure.message(): String =
  when (this) {
    NotZip -> Strings.budgetImportErrorNotZip
    MissingFiles -> Strings.budgetImportErrorMissingFiles
    BadMetadata -> Strings.budgetImportErrorBadMetadata
    Unknown -> Strings.budgetImportErrorUnknown
  }

@Preview
@Composable
private fun PreviewImportBudgetDialog(
  @PreviewParameter(ImportBudgetDialogProvider::class) params: ColorParams<ImportBudgetState>,
) =
  PreviewWithColorParams(params) {
    ImportBudgetDialogContent(state = params.data, onSelectFile = {}, onDismiss = {})
  }

private class ImportBudgetDialogProvider :
  ColorParamsProvider<ImportBudgetState>(
    ImportBudgetState.Idle,
    ImportBudgetState.Importing,
    ImportBudgetState.Failed(MissingFiles),
  )
