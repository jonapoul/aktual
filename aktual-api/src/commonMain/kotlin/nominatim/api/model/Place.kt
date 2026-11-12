package nominatim.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Place(
  @SerialName("name") val name: String? = null,
  @SerialName("display_name") val displayName: String? = null,
  @SerialName("address") val address: Address? = null,
  // Set instead of the rest when nothing is there, like out at sea
  @SerialName("error") val error: String? = null,
)

@Serializable
data class Address(
  @SerialName("house_number") val houseNumber: String? = null,
  @SerialName("road") val road: String? = null,
  @SerialName("suburb") val suburb: String? = null,
  @SerialName("hamlet") val hamlet: String? = null,
  @SerialName("village") val village: String? = null,
  @SerialName("town") val town: String? = null,
  @SerialName("city") val city: String? = null,
  @SerialName("postcode") val postcode: String? = null,
  @SerialName("country") val country: String? = null,
)
