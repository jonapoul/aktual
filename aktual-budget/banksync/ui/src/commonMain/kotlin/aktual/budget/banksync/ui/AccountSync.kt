package aktual.budget.banksync.ui

import androidx.compose.runtime.Immutable

/** Syncing one account from its row, [enabled] unless another sync is running. */
@Immutable internal data class AccountSync(val enabled: Boolean, val onClick: () -> Unit)
