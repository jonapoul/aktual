package aktual.budget.banksync.components

import aktual.budget.banksync.domain.BankSyncSummary
import aktual.budget.banksync.domain.LinkedTransactions
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
  onViewAdded: (LinkedTransactions) -> Unit,
) {
  val message =
    when (summary) {
      is Synced ->
        getString(
          Res.string.bank_sync_result_synced,
          name(summary.name),
          summary.added,
          summary.updated,
        )

      is Failed -> getString(Res.string.bank_sync_result_failed, name(summary.name))

      is Several ->
        if (summary.failed == 0) {
          getString(
            Res.string.bank_sync_result_several,
            summary.synced,
            summary.added,
            summary.updated,
          )
        } else {
          getString(
            Res.string.bank_sync_result_several_failed,
            summary.synced,
            summary.synced + summary.failed,
            summary.added,
            summary.updated,
          )
        }
    }
  val linked = summary.linked
  val actionLabel = linked?.let { getString(Res.string.bank_sync_result_view) }
  val duration = if (actionLabel == null) Short else Long
  val result =
    when {
      summary is Failed -> showErrorSnackbar(message)
      summary is Several && summary.failed > 0 ->
        showWarningSnackbar(message, actionLabel, withDismissAction = true, duration = duration)
      else ->
        showSuccessSnackbar(message, actionLabel, withDismissAction = true, duration = duration)
    }
  if (linked != null && result == ActionPerformed) onViewAdded(linked)
}

private suspend fun name(name: String?) = name ?: getString(Res.string.bank_sync_unnamed_account)
