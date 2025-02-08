package actual.api.model

import alakazam.kotlin.serialization.SerializableByString
import alakazam.kotlin.serialization.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(ResponseStatus.Serializer::class)
enum class ResponseStatus(override val value: String) : SerializableByString {
  Ok(value = "ok"),
  Error(value = "error"),
  ;

  internal object Serializer : KSerializer<ResponseStatus> by enumStringSerializer()
}
