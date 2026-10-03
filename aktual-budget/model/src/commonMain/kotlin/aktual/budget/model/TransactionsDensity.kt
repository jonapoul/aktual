package aktual.budget.model

enum class TransactionsDensity(private val value: String) {
  Comfortable("Comfortable"),
  Compact("Compact"),
  Dense("Dense");

  override fun toString(): String = value

  companion object {
    val Default = Compact
  }
}
