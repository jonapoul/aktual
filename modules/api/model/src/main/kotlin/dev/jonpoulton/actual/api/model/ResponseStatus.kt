package dev.jonpoulton.actual.api.model

import alakazam.kotlin.serialization.SerializableByString
import dev.jonpoulton.actual.api.model.internal.ResponseStatusSerializer
import kotlinx.serialization.Serializable

@Serializable(ResponseStatusSerializer::class)
enum class ResponseStatus(override val value: String) : SerializableByString {
  Ok(value = "ok"),
  Error(value = "error"),
}
