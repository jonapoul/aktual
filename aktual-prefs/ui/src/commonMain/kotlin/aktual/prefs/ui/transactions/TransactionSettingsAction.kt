package aktual.prefs.ui.transactions

import androidx.compose.runtime.Immutable

internal sealed interface TransactionSettingsAction

internal data object NavBack : TransactionSettingsAction

@Immutable
internal fun interface TransactionSettingsActionHandler {
  operator fun invoke(action: TransactionSettingsAction)
}
