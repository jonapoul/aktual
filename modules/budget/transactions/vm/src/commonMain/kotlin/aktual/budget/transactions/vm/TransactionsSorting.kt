/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.vm

import aktual.budget.model.DbMetadata
import aktual.budget.model.SortColumn
import aktual.budget.model.SortDirection
import androidx.compose.runtime.Immutable

@Immutable
data class TransactionsSorting(
  val column: SortColumn,
  val direction: SortDirection,
) {
  companion object {
    val Default = TransactionsSorting(SortColumn.Default, SortDirection.Default)
  }
}

fun TransactionsSorting(metadata: DbMetadata) = TransactionsSorting(
  column = metadata[SortColumnDelegate],
  direction = metadata[SortDirectionDelegate],
)
