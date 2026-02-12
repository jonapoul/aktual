package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(RuleStage.Serializer::class)
enum class RuleStage(override val value: String) : SerializableByString {
  Pre(value = "pre"),
  Post(value = "post");

  override fun toString(): String = value

  internal object Serializer : KSerializer<RuleStage> by enumStringSerializer()
}
