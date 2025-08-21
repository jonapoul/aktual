@file:Suppress(
  "MatchingDeclarationName",
  "ktlint:standard:filename",
)

package actual.api.model.account

import actual.api.model.internal.BoolAsInt
import actual.core.model.LoginMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvailableLoginMethod(
  @SerialName("method") val method: LoginMethod,
  @SerialName("displayName") val displayName: String,
  @SerialName("active") val active: BoolAsInt,
)
