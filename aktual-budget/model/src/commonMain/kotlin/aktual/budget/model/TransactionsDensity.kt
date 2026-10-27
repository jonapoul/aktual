package aktual.budget.model

enum class TransactionsDensity {
  Comfortable,
  Compact,
  Dense;

  companion object {
    val Default = Compact
  }
}
