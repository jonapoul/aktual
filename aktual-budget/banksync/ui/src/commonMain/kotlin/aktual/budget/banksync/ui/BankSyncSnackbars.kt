package aktual.budget.banksync.ui

import aktual.budget.banksync.domain.BankSyncSummary
import aktual.budget.banksync.vm.providers.SetupError
import aktual.core.l10n.Res
import aktual.core.l10n.bank_sync_failure_message
import aktual.core.l10n.bank_sync_link_failed
import aktual.core.l10n.bank_sync_providers_reset_done
import aktual.core.l10n.bank_sync_providers_reset_failed
import aktual.core.l10n.bank_sync_result_failed
import aktual.core.l10n.bank_sync_result_several
import aktual.core.l10n.bank_sync_result_several_failed
import aktual.core.l10n.bank_sync_result_synced
import aktual.core.l10n.bank_sync_settings_save_failed
import aktual.core.l10n.bank_sync_settings_unlink_failed
import aktual.core.l10n.bank_sync_setup_failed
import aktual.core.l10n.bank_sync_setup_logged_out
import aktual.core.l10n.bank_sync_setup_not_admin
import aktual.core.l10n.bank_sync_unnamed_account
import androidx.compose.material3.SnackbarHostState
import org.jetbrains.compose.resources.getString

suspend fun SnackbarHostState.showBankSyncSummary(summary: BankSyncSummary) {
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
  showSnackbar(message)
}

private suspend fun name(name: String?) = name ?: getString(Res.string.bank_sync_unnamed_account)

internal suspend fun SnackbarHostState.showSettingsSaveFailed(cause: String?) =
  showSnackbar(
    getString(
      Res.string.bank_sync_settings_save_failed,
      cause ?: getString(Res.string.bank_sync_failure_message),
    ),
  )

internal suspend fun SnackbarHostState.showUnlinkFailed(cause: String?) =
  showSnackbar(
    getString(
      Res.string.bank_sync_settings_unlink_failed,
      cause ?: getString(Res.string.bank_sync_failure_message),
    ),
  )

internal suspend fun SnackbarHostState.showLinkFailed(cause: String?) =
  showSnackbar(
    getString(
      Res.string.bank_sync_link_failed,
      cause ?: getString(Res.string.bank_sync_failure_message),
    ),
  )

internal suspend fun SnackbarHostState.showProviderReset() =
  showSnackbar(getString(Res.string.bank_sync_providers_reset_done))

internal suspend fun SnackbarHostState.showProviderResetFailed(error: SetupError) {
  val reason =
    when (error) {
      NotAdmin -> getString(Res.string.bank_sync_setup_not_admin)
      LoggedOut -> getString(Res.string.bank_sync_setup_logged_out)
      is Other -> error.cause ?: getString(Res.string.bank_sync_setup_failed)
    }
  showSnackbar(getString(Res.string.bank_sync_providers_reset_failed, reason))
}
