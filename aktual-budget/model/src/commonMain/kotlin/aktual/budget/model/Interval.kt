package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(Interval.Serializer::class)
enum class Interval(override val value: String) : SerializableByString {
  Daily("Daily"),
  Weekly("Weekly"),
  Monthly("Monthly"),
  Yearly("Yearly");

  override fun toString(): String = value

  object Serializer : KSerializer<Interval> by enumStringSerializer()
}
