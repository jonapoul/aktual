package aktual.budget.navrail.ui

import aktual.budget.navrail.vm.UploadBudgetState
import aktual.budget.navrail.vm.UploadBudgetViewModel
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
internal fun UploadBudgetDialog(
  onDismiss: () -> Unit,
  viewModel: UploadBudgetViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsState()
  val close = {
    viewModel.reset()
    onDismiss()
  }

  SideEffect(state) { if (state is Done) close() }

  AktualAlertDialog(onDismissRequest = close) {
    UploadBudgetDialogContent(
      state = state,
      serverHost = viewModel.serverHost.orEmpty(),
      onUpload = viewModel::upload,
      onDismiss = close,
    )
  }
}

@Composable
private fun UploadBudgetDialogContent(
  state: UploadBudgetState,
  serverHost: String,
  onUpload: () -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialogContent(
    title = Strings.budgetNavMenuUploadBudget,
    buttons = {
      DialogTextButton(text = Strings.budgetUploadCancel, onClick = onDismiss)
      DialogTextButton(
        kind = Primary,
        text = Strings.budgetUploadConfirm,
        isEnabled = state !is Uploading,
        onClick = onUpload,
      )
    },
    content = {
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = spacedBy(Dimens.Large)) {
        Text(
          text = Strings.budgetUploadDescription(serverHost),
          style = typography.bodyMedium,
          color = colors.pageText,
        )

        if (state is Failed) {
          Text(
            text = state.failure.message(),
            style = typography.bodyMedium,
            color = colors.errorText,
          )
        }

        if (state is Uploading) {
          AktualProgressIndicator(modifier = Modifier.align(CenterHorizontally), size = 32.dp)
        }
      }
    },
  )

@Composable
private fun UploadResult.Failure.message(): String =
  when (this) {
    NoServer -> Strings.budgetUploadErrorNoServer
    Encrypted -> Strings.budgetUploadErrorEncrypted
    is Rejected -> Strings.budgetUploadErrorRejected(reason)
    Unreachable -> Strings.budgetUploadErrorUnreachable
  }

@Preview
@Composable
private fun PreviewUploadBudgetDialog(
  @PreviewParameter(UploadBudgetDialogProvider::class) params: ColorParams<UploadBudgetState>,
) =
  PreviewWithColorParams(params) {
    UploadBudgetDialogContent(
      state = params.data,
      serverHost = "budget.example.com",
      onUpload = {},
      onDismiss = {},
    )
  }

private class UploadBudgetDialogProvider :
  ColorParamsProvider<UploadBudgetState>(
    UploadBudgetState.Idle,
    UploadBudgetState.Uploading,
    UploadBudgetState.Failed(UploadResult.Rejected("unauthorized")),
  )
