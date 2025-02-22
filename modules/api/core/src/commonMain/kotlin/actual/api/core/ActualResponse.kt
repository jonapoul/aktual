package actual.api.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import okhttp3.Headers

data class ActualResponse<T : Any>(
  val body: T,
  val code: Int,
  val message: String,
  val headers: Headers,
  val isSuccessful: Boolean,
)

typealias RetrofitResponse<T> = retrofit2.Response<T>

fun <Base : Any, Success : Base, Failure : Base> RetrofitResponse<Success>.adapted(
  format: StringFormat,
  failureSerializer: KSerializer<Failure>,
): ActualResponse<Base> = if (isSuccessful) {
  adapt(body())
} else {
  val failure = errorBody()
    ?.string()
    ?.let { format.decodeFromString(failureSerializer, it) }
  adapt(failure)
}

private fun <T : Any> RetrofitResponse<*>.adapt(body: T?) = ActualResponse(
  body = body ?: error("Null body in $this"),
  code = code(),
  message = message(),
  headers = headers(),
  isSuccessful = isSuccessful,
)
