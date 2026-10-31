package aktual.budget.banksync.ui

import aktual.budget.banksync.vm.providers.SetupError
import aktual.core.l10n.Res
import aktual.core.l10n.bank_sync_failure_message
import aktual.core.l10n.bank_sync_link_failed
import aktual.core.l10n.bank_sync_providers_reset_done
import aktual.core.l10n.bank_sync_providers_reset_failed
import aktual.core.l10n.bank_sync_settings_save_failed
import aktual.core.l10n.bank_sync_settings_unlink_failed
import aktual.core.l10n.bank_sync_setup_failed
import aktual.core.l10n.bank_sync_setup_logged_out
import aktual.core.l10n.bank_sync_setup_not_admin
import androidx.compose.material3.SnackbarHostState
import org.jetbrains.compose.resources.getString

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
