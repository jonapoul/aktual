package aktual.budget.model

import alakazam.kotlin.SerializableByString

/** packages/loot-core/src/server/dashboard/app.ts, exportModel() */
enum class WidgetType(override val value: String) : SerializableByString {
  NetWorth("net-worth-card"),
  CashFlow("cash-flow-card"),
  Spending("spending-card"),
  Custom("custom-report"),
  Markdown("markdown-card"),
  Summary("summary-card"),
  Calendar("calendar-card"),
  BudgetAnalysis("budget-analysis-card"),
  Formula("formula-card");

  override fun toString(): String = value
}
