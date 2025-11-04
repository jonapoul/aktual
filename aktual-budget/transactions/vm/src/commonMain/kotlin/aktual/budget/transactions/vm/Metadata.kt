/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.vm

import aktual.budget.model.DbMetadata
import aktual.budget.model.SortColumn
import aktual.budget.model.SortDirection
import aktual.budget.model.TransactionsFormat

internal val SortColumnKey = DbMetadata.enumKey<SortColumn>("sortColumn")
internal val SortDirectionKey = DbMetadata.enumKey<SortDirection>("sortDirection")
internal val TransactionFormatKey = DbMetadata.enumKey<TransactionsFormat>("transactionFormat")
