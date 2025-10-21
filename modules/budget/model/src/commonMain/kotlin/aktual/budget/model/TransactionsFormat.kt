/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class TransactionsFormat {
  List,
  Table,
  ;

  companion object {
    val Default = List
  }
}
