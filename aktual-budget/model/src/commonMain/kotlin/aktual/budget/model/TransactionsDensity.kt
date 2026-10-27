package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionsDensity {
  Comfortable,
  @Fallback Compact,
  Dense;

  companion object {
    val Default = Compact
  }
}
