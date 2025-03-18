package actual.api.client

import kotlinx.serialization.KSerializer
import okhttp3.Headers
import retrofit2.Response

data class ActualResponse<T : Any>(
  val body: T,
  val code: Int,
  val message: String,
  val headers: Headers,
  val isSuccessful: Boolean,
)

internal fun <Base : Any, Success : Base, Failure : Base> Response<Success>.adapted(
  failureSerializer: KSerializer<Failure>,
): ActualResponse<Base> = if (isSuccessful) {
  adapt(body())
} else {
  val failure = errorBody()
    ?.string()
    ?.let { ActualJson.decodeFromString(failureSerializer, it) }
  adapt(failure)
}

private fun <T : Any> Response<*>.adapt(body: T?) = ActualResponse(
  body = body ?: error("Null body in $this"),
  code = code(),
  message = message(),
  headers = headers(),
  isSuccessful = isSuccessful,
)
