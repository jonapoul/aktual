/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class SortColumn {
  Date,
  Account,
  Payee,
  Notes,
  Category,
  Amount,
  ;

  companion object {
    val Default = Date
  }
}

enum class SortDirection {
  Ascending,
  Descending,
  ;

  companion object {
    val Default = Descending
  }
}
