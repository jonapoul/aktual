/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class GroupBy(private val value: String) {
  Account(value = "Account"),
  Category(value = "Category"),
  Group(value = "Group"),
  Interval(value = "Interval"),
  Payee(value = "Payee"),
  ;

  override fun toString(): String = value
}
