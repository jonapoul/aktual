/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class BalanceType(private val value: String) {
  Deposit(value = "Deposit"),
  Expense(value = "Expense"), // not used anywhere?
  Net(value = "Net"),
  NetDeposit(value = "Net Deposit"),
  NetPayment(value = "Net Payment"),
  Payment(value = "Payment"),
  ;

  override fun toString(): String = value
}
