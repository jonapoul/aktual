package aktual.budget.model

import alakazam.kotlin.SerializableByString

enum class BalanceType(override val value: String) : SerializableByString {
  Deposit(value = "Deposit"),
  Expense(value = "Expense"), // not used anywhere?
  Net(value = "Net"),
  NetDeposit(value = "Net Deposit"),
  NetPayment(value = "Net Payment"),
  Payment(value = "Payment");

  override fun toString(): String = value
}
