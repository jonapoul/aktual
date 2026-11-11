package aktual.budget.apikeys.ui

import aktual.budget.apikeys.vm.ApiKeyItem
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface ApiKeysAction

internal data object NavBack : ApiKeysAction

internal data object Retry : ApiKeysAction

internal data object NewKey : ApiKeysAction

@JvmInline internal value class Revoke(val key: ApiKeyItem) : ApiKeysAction
