package dev.jonpoulton.actual.api.model.account

import dev.jonpoulton.actual.api.model.Response
import dev.jonpoulton.actual.api.model.ResponseStatus
import dev.jonpoulton.actual.api.model.internal.NeedsBootstrapResponseSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(NeedsBootstrapResponseSerializer::class)
sealed interface NeedsBootstrapResponse : Response {
  @Serializable
  data class Ok(
    @SerialName("data") val data: Data,
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Ok,
  ) : NeedsBootstrapResponse

  @Serializable
  data class Error(
    @SerialName("reason") val reason: String,
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Error,
  ) : NeedsBootstrapResponse

  @Serializable
  data class Data(
    @SerialName("bootstrapped")
    val bootstrapped: Boolean,
  )
}
