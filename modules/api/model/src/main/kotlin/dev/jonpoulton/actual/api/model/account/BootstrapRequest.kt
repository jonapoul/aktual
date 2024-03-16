package dev.jonpoulton.actual.api.model.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.afanasev.sekret.Secret

@Serializable
data class BootstrapRequest(
  @Secret
  @SerialName("password")
  val password: String,
)
