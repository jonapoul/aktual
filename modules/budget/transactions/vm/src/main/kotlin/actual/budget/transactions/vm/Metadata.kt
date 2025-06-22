package actual.budget.transactions.vm

import actual.budget.model.DbMetadata.Companion.enumDelegate
import actual.budget.model.SortColumn
import actual.budget.model.SortDirection
import actual.budget.model.TransactionsFormat

internal val SortColumnDelegate = enumDelegate("sortColumn", SortColumn.Default)
internal val SortDirectionDelegate = enumDelegate("sortDirection", SortDirection.Default)
internal val TransactionFormatDelegate = enumDelegate("transactionFormat", TransactionsFormat.Default)
