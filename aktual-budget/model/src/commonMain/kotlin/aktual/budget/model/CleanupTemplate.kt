package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

// packages/loot-core/src/types/models/cleanup-templates.ts
@Serializable(CleanupTemplateSerializer::class)
sealed interface CleanupTemplate {
  val role: Role

  @Serializable
  data class Source(
    @SerialName("groupId") val groupId: CleanupGroupId?,
    @SerialName("role") override val role: Role = Role.Source,
  ) : CleanupTemplate

  @Serializable
  data class Sink(
    @SerialName("groupId") val groupId: CleanupGroupId?,
    @SerialName("weight") val weight: Int,
    @SerialName("role") override val role: Role = Role.Sink,
  ) : CleanupTemplate

  @Serializable
  data class Overspend(
    @SerialName("groupId") val groupId: CleanupGroupId,
    @SerialName("role") override val role: Role = Role.Overspend,
  ) : CleanupTemplate

  @Serializable
  enum class Role {
    @SerialName("source") Source,
    @SerialName("sink") Sink,
    @SerialName("overspend") Overspend,
    @Fallback Unknown,
  }
}

internal class CleanupTemplateSerializer :
  JsonContentPolymorphicSerializer<CleanupTemplate>(CleanupTemplate::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CleanupTemplate> {
    val role =
      element.jsonObject["role"]?.let {
        Json.decodeFromJsonElement(CleanupTemplate.Role.serializer(), it)
      }
    return when (role) {
      Source -> CleanupTemplate.Source.serializer()
      Sink -> CleanupTemplate.Sink.serializer()
      Overspend -> CleanupTemplate.Overspend.serializer()
      Unknown,
      null -> throw SerializationException("No valid role found in $element")
    }
  }
}
