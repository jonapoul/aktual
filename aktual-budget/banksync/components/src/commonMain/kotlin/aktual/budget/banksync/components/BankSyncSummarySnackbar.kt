package aktual.budget.banksync.components

import aktual.budget.banksync.domain.BankSyncSummary
import aktual.budget.model.TransactionId
import aktual.core.l10n.Res
import aktual.core.l10n.bank_sync_result_failed
import aktual.core.l10n.bank_sync_result_several
import aktual.core.l10n.bank_sync_result_several_failed
import aktual.core.l10n.bank_sync_result_synced
import aktual.core.l10n.bank_sync_result_view
import aktual.core.l10n.bank_sync_unnamed_account
import aktual.core.ui.showErrorSnackbar
import aktual.core.ui.showSuccessSnackbar
import aktual.core.ui.showWarningSnackbar
import androidx.compose.material3.SnackbarDuration.Long
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import org.jetbrains.compose.resources.getString

// Links to what the sync added, if anything, and stays up longer to give time to tap it
suspend fun SnackbarHostState.showBankSyncSummary(
  summary: BankSyncSummary,
  onViewAdded: (List<TransactionId>) -> Unit,
) {
  val message =
    when (summary) {
      is Synced ->
        getString(
          Res.string.bank_sync_result_synced,
          name(summary.name),
          summary.added.size,
          summary.updated,
        )

      is Failed -> getString(Res.string.bank_sync_result_failed, name(summary.name))

      is Several ->
        if (summary.failed == 0) {
          getString(
            Res.string.bank_sync_result_several,
            summary.synced,
            summary.added.size,
            summary.updated,
          )
        } else {
          getString(
            Res.string.bank_sync_result_several_failed,
            summary.synced,
            summary.synced + summary.failed,
            summary.added.size,
            summary.updated,
          )
        }
    }
  val actionLabel =
    getString(Res.string.bank_sync_result_view).takeIf { summary.added.isNotEmpty() }
  val duration = if (actionLabel == null) Short else Long
  val result =
    when {
      summary is Failed -> showErrorSnackbar(message)
      summary is Several && summary.failed > 0 ->
        showWarningSnackbar(message, actionLabel, withDismissAction = true, duration = duration)
      else ->
        showSuccessSnackbar(message, actionLabel, withDismissAction = true, duration = duration)
    }
  if (result == ActionPerformed) onViewAdded(summary.added)
}

private suspend fun name(name: String?) = name ?: getString(Res.string.bank_sync_unnamed_account)
