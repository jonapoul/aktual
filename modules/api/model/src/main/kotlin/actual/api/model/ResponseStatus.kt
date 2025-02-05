package actual.api.model

import actual.api.model.internal.ResponseStatusSerializer
import alakazam.kotlin.serialization.SerializableByString
import kotlinx.serialization.Serializable

@Serializable(ResponseStatusSerializer::class)
enum class ResponseStatus(
  override val value: String,
) : SerializableByString {
  Ok(value = "ok"),
  Error(value = "error"),
}
