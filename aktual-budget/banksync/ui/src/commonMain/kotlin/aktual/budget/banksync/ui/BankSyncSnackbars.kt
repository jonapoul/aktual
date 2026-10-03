package aktual.budget.banksync.ui

import aktual.budget.banksync.domain.BankSyncSummary
import aktual.core.l10n.Res
import aktual.core.l10n.bank_sync_result_failed
import aktual.core.l10n.bank_sync_result_several
import aktual.core.l10n.bank_sync_result_several_failed
import aktual.core.l10n.bank_sync_result_synced
import aktual.core.l10n.bank_sync_unnamed_account
import androidx.compose.material3.SnackbarHostState
import org.jetbrains.compose.resources.getString

/** What a finished bank sync did, e.g. "Checking: 5 new, 3 updated". */
suspend fun SnackbarHostState.showBankSyncSummary(summary: BankSyncSummary) {
  val message =
    when (summary) {
      is BankSyncSummary.Synced ->
        getString(
          Res.string.bank_sync_result_synced,
          name(summary.name),
          summary.added,
          summary.updated,
        )
      is BankSyncSummary.Failed -> getString(Res.string.bank_sync_result_failed, name(summary.name))
      is BankSyncSummary.Several ->
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
  showSnackbar(message)
}

private suspend fun name(name: String?) = name ?: getString(Res.string.bank_sync_unnamed_account)
