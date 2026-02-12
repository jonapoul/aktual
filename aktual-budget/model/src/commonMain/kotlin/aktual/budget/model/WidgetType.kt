package aktual.budget.model

/** packages/loot-core/src/server/dashboard/app.ts, exportModel() */
enum class WidgetType(private val value: String) {
  NetWorth("net-worth-card"),
  CashFlow("cash-flow-card"),
  Spending("spending-card"),
  Custom("custom-report"),
  Markdown("markdown-card"),
  Summary("summary-card"),
  Calendar("calendar-card");

  override fun toString(): String = value
}
