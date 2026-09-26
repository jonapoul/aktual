package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Interval {
  @SerialName("Daily") Daily,
  @SerialName("Weekly") Weekly,
  @SerialName("Monthly") Monthly,
  @SerialName("Yearly") Yearly,
  @Fallback Unknown;

  companion object {
    val known: ImmutableList<Interval> = entries.filter { it != Unknown }.toImmutableList()
  }
}
