package aktual.budget.model

import alakazam.kotlin.SerializableByString
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
  enum class Role(override val value: String) : SerializableByString {
    Source("source"),
    Sink("sink"),
    Overspend("overspend"),
  }
}

internal class CleanupTemplateSerializer :
  JsonContentPolymorphicSerializer<CleanupTemplate>(CleanupTemplate::class) {
  override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CleanupTemplate> {
    val string = element.jsonObject["role"]?.jsonPrimitive?.contentOrNull
    val role = CleanupTemplate.Role.entries.firstOrNull { it.value == string }
    return when (role) {
      CleanupTemplate.Role.Source -> CleanupTemplate.Source.serializer()
      CleanupTemplate.Role.Sink -> CleanupTemplate.Sink.serializer()
      CleanupTemplate.Role.Overspend -> CleanupTemplate.Overspend.serializer()
      null -> throw SerializationException("No role found in $element")
    }
  }
}
