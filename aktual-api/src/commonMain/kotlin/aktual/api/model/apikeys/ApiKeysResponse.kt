package aktual.api.model.apikeys

sealed interface ApiKeysResponse<out T> {
  data class Success<T>(val data: T) : ApiKeysResponse<T>

  data class Failed(val reason: String?) : ApiKeysResponse<Nothing>

  companion object {
    const val INVALID_API_KEY = "invalid-api-key"
    const val FILE_ACCESS_DENIED = "file-access-denied"
    const val API_KEY_NOT_FOUND = "api-key-not-found"

    // Not from the server: it didn't reply with JSON, so likely doesn't have these endpoints
    const val UNSUPPORTED = "unsupported"
  }
}
