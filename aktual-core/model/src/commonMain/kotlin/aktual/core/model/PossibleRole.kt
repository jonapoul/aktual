package aktual.core.model

import alakazam.kotlin.serialization.SerializableByString
import alakazam.kotlin.serialization.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(PossibleRole.Serializer::class)
enum class PossibleRole(override val value: String) : SerializableByString {
  Admin("ADMIN"),
  Basic("BASIC"),
  ;

  internal object Serializer : KSerializer<PossibleRole> by enumStringSerializer()
}
