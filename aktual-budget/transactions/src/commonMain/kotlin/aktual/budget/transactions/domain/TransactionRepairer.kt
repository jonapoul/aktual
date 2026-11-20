package aktual.budget.transactions.domain

import kotlinx.datetime.LocalDate

/**
 * packages/loot-core/src/server/tools/app.ts fixSplitTransactions(), behind the "Repair
 * transactions" button of upstream's advanced settings.
 */
interface TransactionRepairer {
  suspend fun repair(): RepairResult
}

data class RepairResult(
  val blankPayees: Int,
  val cleared: Int,
  val deleted: Int,
  val transfers: Int,
  val nonParentErrors: Int,
  val categorisedParents: Int,
  // The dates of split parents whose parts don't add up, for the user to fix by hand
  val mismatchedSplits: List<LocalDate>,
) {
  val isEmpty: Boolean
    get() =
      blankPayees == 0 &&
        cleared == 0 &&
        deleted == 0 &&
        transfers == 0 &&
        nonParentErrors == 0 &&
        categorisedParents == 0 &&
        mismatchedSplits.isEmpty()
}
