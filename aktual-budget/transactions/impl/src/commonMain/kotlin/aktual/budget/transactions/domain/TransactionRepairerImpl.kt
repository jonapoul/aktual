package aktual.budget.transactions.domain

import aktual.budget.db.dao.RepairTransactionsDao
import aktual.budget.model.TransactionId
import aktual.di.BudgetScope
import aktual.log
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

// Each step is its own write, as upstream runs a mutator per step, so later ones see earlier fixes
@Inject
@ContributesBinding(BudgetScope::class)
class TransactionRepairerImpl
internal constructor(
  private val dao: RepairTransactionsDao,
  private val writer: TransactionWriter,
) : TransactionRepairer {
  override suspend fun repair(): RepairResult {
    val blankPayees = dao.blankPayees()
    write {
      blankPayees.forEach { row ->
        update(TransactionUpdate(id = row.id, payee = Patch.To(row.parentPayee)))
      }
    }

    val cleared = dao.mismatchedCleared()
    write { cleared.forEach { row -> update(TransactionUpdate(row.id, cleared = row.cleared == true)) } }

    val deleted = dao.orphanedChildren()
    write { deleted.forEach { id -> delete(id) } }

    val mismatchedSplits = dao.mismatchedSplits()

    val transfers = dao.categorisedTransfers()
    write { clearCategories(transfers) }

    val errors = dao.nonParentErrors()
    write { errors.forEach { id -> update(TransactionUpdate(id, error = Patch.To(null))) } }

    val parents = dao.categorisedParents()
    write { clearCategories(parents) }

    val result =
      RepairResult(
        blankPayees = blankPayees.size,
        cleared = cleared.size,
        deleted = deleted.size,
        transfers = transfers.size,
        nonParentErrors = errors.size,
        categorisedParents = parents.size,
        mismatchedSplits = mismatchedSplits.mapNotNull { it.date },
      )
    log.i { "Repaired transactions: $result" }
    return result
  }

  // batchUpdateTransactions() runs transfers by default
  private suspend fun write(block: suspend TransactionBatch.() -> Unit) =
    writer.write(runTransfers = true, block = block)

  private suspend fun TransactionBatch.clearCategories(ids: List<TransactionId>) =
    ids.forEach { id -> update(TransactionUpdate(id, category = Patch.To(null))) }
}
