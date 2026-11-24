package aktual.budget.navrail.ui

import aktual.budget.navrail.vm.MissingBudgetState
import aktual.budget.navrail.vm.MissingBudgetViewModel
import aktual.budget.utils.UploadResult
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
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel

// Shown when the server no longer has the open budget, until the user picks what to do with it
@Composable
internal fun MissingBudgetDialog(
  onClose: () -> Unit,
  viewModel: MissingBudgetViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsState()
  val shown = state as? Shown ?: return

  // Not dismissable, since leaving it open would keep failing to sync
  AktualAlertDialog(onDismissRequest = {}) {
    MissingBudgetDialogContent(
      state = shown,
      serverHost = viewModel.serverHost.orEmpty(),
      onReupload = viewModel::reupload,
      onKeepLocal = viewModel::keepLocal,
      onClose = onClose,
    )
  }
}

@Composable
private fun MissingBudgetDialogContent(
  state: MissingBudgetState.Shown,
  serverHost: String,
  onReupload: () -> Unit,
  onKeepLocal: () -> Unit,
  onClose: () -> Unit,
) =
  AktualAlertDialogContent(
    title = Strings.budgetMissingTitle,
    buttons = {
      DialogTextButton(
        text = Strings.budgetMissingClose,
        isEnabled = !state.isUploading,
        onClick = onClose,
      )
      DialogTextButton(
        text = Strings.budgetMissingKeepLocal,
        isEnabled = !state.isUploading,
        onClick = onKeepLocal,
      )
      if (state.canReupload) {
        DialogTextButton(
          kind = Primary,
          text = Strings.budgetMissingReupload,
          isEnabled = !state.isUploading,
          onClick = onReupload,
        )
      }
    },
    content = {
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = spacedBy(Dimens.Large)) {
        Text(
          text = Strings.budgetMissingDescription(serverHost),
          style = typography.bodyMedium,
          color = colors.pageText,
        )

        if (!state.canReupload) {
          Text(
            text = Strings.budgetMissingEncrypted,
            style = typography.bodyMedium,
            color = colors.pageText,
          )
        }

        state.failure?.let { failure ->
          Text(
            text = failure.message(),
            style = typography.bodyMedium,
            color = colors.errorText,
          )
        }

        if (state.isUploading) {
          AktualProgressIndicator(modifier = Modifier.align(CenterHorizontally), size = 32.dp)
        }
      }
    },
  )

@Preview
@Composable
private fun PreviewMissingBudgetDialog(
  @PreviewParameter(MissingBudgetDialogProvider::class)
  params: ColorParams<MissingBudgetState.Shown>,
) =
  PreviewWithColorParams(params) {
    MissingBudgetDialogContent(
      state = params.data,
      serverHost = "budget.example.com",
      onReupload = {},
      onKeepLocal = {},
      onClose = {},
    )
  }

private class MissingBudgetDialogProvider :
  ColorParamsProvider<MissingBudgetState.Shown>(
    MissingBudgetState.Shown(canReupload = true),
    MissingBudgetState.Shown(canReupload = false),
    MissingBudgetState.Shown(canReupload = true, isUploading = true),
    MissingBudgetState.Shown(canReupload = true, failure = UploadResult.Unreachable),
  )
