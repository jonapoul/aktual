package aktual.budget.banksync.ui

import aktual.budget.banksync.domain.BankSyncSummary
import aktual.core.l10n.Res
import aktual.core.l10n.bank_sync_failure_message
import aktual.core.l10n.bank_sync_link_failed
import aktual.core.l10n.bank_sync_result_failed
import aktual.core.l10n.bank_sync_result_several
import aktual.core.l10n.bank_sync_result_several_failed
import aktual.core.l10n.bank_sync_result_synced
import aktual.core.l10n.bank_sync_settings_save_failed
import aktual.core.l10n.bank_sync_settings_unlink_failed
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

/** Why the bank sync settings couldn't be saved */
internal suspend fun SnackbarHostState.showSettingsSaveFailed(cause: String?) =
  showSnackbar(
    getString(
      Res.string.bank_sync_settings_save_failed,
      cause ?: getString(Res.string.bank_sync_failure_message),
    )
  )

/** Why the account couldn't be unlinked */
internal suspend fun SnackbarHostState.showUnlinkFailed(cause: String?) =
  showSnackbar(
    getString(
      Res.string.bank_sync_settings_unlink_failed,
      cause ?: getString(Res.string.bank_sync_failure_message),
    )
  )

/** Why the account couldn't be linked */
internal suspend fun SnackbarHostState.showLinkFailed(cause: String?) =
  showSnackbar(
    getString(
      Res.string.bank_sync_link_failed,
      cause ?: getString(Res.string.bank_sync_failure_message),
    )
  )
