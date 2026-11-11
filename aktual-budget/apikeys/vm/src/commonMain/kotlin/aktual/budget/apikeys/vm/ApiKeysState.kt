package aktual.budget.apikeys.vm

import aktual.api.model.apikeys.ApiKeyId
import aktual.core.model.Token
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Immutable data class ApiKeyItem(val id: ApiKeyId, val name: String, val created: LocalDate)

@Immutable sealed interface ApiKeysState

data object Loading : ApiKeysState

// The server doesn't have the API key endpoints, or this budget isn't on a server
data object Unsupported : ApiKeysState

@JvmInline value class Failure(val cause: String?) : ApiKeysState

data object Empty : ApiKeysState

@JvmInline value class Loaded(val keys: ImmutableList<ApiKeyItem>) : ApiKeysState

@Immutable sealed interface ApiKeysDialog

data class CreateKey(val isBusy: Boolean = false, val hasFailed: Boolean = false) : ApiKeysDialog

// The server only shows the key once, so this is the user's one chance to copy it
@JvmInline value class CreatedKey(val key: Token) : ApiKeysDialog

data class RevokeKey(
  val key: ApiKeyItem,
  val isBusy: Boolean = false,
  val hasFailed: Boolean = false,
) : ApiKeysDialog
