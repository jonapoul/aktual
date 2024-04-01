package dev.jonpoulton.actual.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthFailureModel(
  @SerialName("status")
  val status: ResponseStatus,

  @SerialName("reason")
  val reason: String,

  @SerialName("details")
  val details: String,
)
