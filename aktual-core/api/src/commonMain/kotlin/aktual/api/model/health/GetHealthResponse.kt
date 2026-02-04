package aktual.api.model.health

import kotlinx.serialization.Serializable

@Serializable
data class GetHealthResponse(
  val status: String = "UP",
)
