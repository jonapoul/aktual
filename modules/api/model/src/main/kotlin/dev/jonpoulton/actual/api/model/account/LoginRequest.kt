package dev.jonpoulton.actual.api.model.account

import dev.jonpoulton.actual.api.model.internal.PasswordSerializer
import dev.jonpoulton.actual.core.model.Password
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
  @SerialName("password")
  @Serializable(with = PasswordSerializer::class)
  val password: Password,
)

