/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.vm

import aktual.budget.model.DbMetadata.Companion.enumDelegate
import aktual.budget.model.SortColumn
import aktual.budget.model.SortDirection
import aktual.budget.model.TransactionsFormat

internal val SortColumnDelegate = enumDelegate("sortColumn", SortColumn.Default)
internal val SortDirectionDelegate = enumDelegate("sortDirection", SortDirection.Default)
internal val TransactionFormatDelegate = enumDelegate("transactionFormat", TransactionsFormat.Default)
