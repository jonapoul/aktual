package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** packages/loot-core/src/server/dashboard/app.ts, exportModel() */
@Serializable
enum class WidgetType {
  @SerialName("net-worth-card") NetWorth,
  @SerialName("cash-flow-card") CashFlow,
  @SerialName("spending-card") Spending,
  @SerialName("custom-report") Custom,
  @SerialName("markdown-card") Markdown,
  @SerialName("summary-card") Summary,
  @SerialName("calendar-card") Calendar,
  @SerialName("budget-analysis-card") BudgetAnalysis,
  @SerialName("formula-card") Formula,
  @Fallback Unknown;

  companion object {
    val known: ImmutableList<WidgetType> = entries.filter { it != Unknown }.toImmutableList()
  }
}
