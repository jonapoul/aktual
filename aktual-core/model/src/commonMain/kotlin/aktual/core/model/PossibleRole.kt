package aktual.core.model

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//@Serializable(with = PossibleRole.FallbackSerializer::class)
@Serializable(PossibleRole.FallbackSerializer::class)
enum class PossibleRole {
  @SerialName("ADMIN") Admin,
  @SerialName("BASIC") Basic,
  @Fallback Unknown,
}
