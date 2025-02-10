package actual.budget.model

enum class BalanceType(private val value: String) {
  Deposit(value = "Deposit"),
  Expense(value = "Expense"), // not used anywhere?
  Net(value = "Net"),
  NetDeposit(value = "Net Deposit"),
  NetPayment(value = "Net Payment"),
  Payment(value = "Payment"),
  ;

  override fun toString(): String = value

  companion object {
    fun fromString(string: String): BalanceType = entries
      .firstOrNull { it.value == string }
      ?: error("No BalanceType matching $string")
  }
}
