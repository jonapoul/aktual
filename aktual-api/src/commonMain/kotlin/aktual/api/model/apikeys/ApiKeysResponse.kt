package aktual.api.model.apikeys

import aktual.api.model.account.FailureReason

sealed interface ApiKeysResponse<out T> {
  data class Success<T>(val data: T) : ApiKeysResponse<T>

  data class Failed(val reason: FailureReason?) : ApiKeysResponse<Nothing>

  // The server didn't reply with JSON, so likely doesn't have these endpoints
  data object Unsupported : ApiKeysResponse<Nothing>
}
