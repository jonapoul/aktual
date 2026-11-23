package aktual.budget.banksync.domain

import aktual.budget.model.TransactionId

sealed interface BankSyncSummary {
  // What the snackbar links to, when the sync added anything that's still there
  val linked: LinkedTransactions?

  data class Synced(
    val name: String?,
    val added: Int,
    val updated: Int,
    override val linked: LinkedTransactions? = null,
  ) : BankSyncSummary

  data class Failed(val name: String?) : BankSyncSummary {
    override val linked: LinkedTransactions?
      get() = null
  }

  data class Several(
    val synced: Int,
    val failed: Int,
    val added: Int,
    val updated: Int,
    override val linked: LinkedTransactions? = null,
  ) : BankSyncSummary

  companion object {
    // Few enough that their IDs fit in the saved nav stack
    const val MAX_LINKED = 500

    /**
     * [sortIds] puts the added transactions in list order and drops deleted ones, so the newest
     * [MAX_LINKED] of them are linked
     */
    suspend fun of(
      results: List<BankSyncResult>,
      sortIds: suspend (List<TransactionId>) -> List<TransactionId>,
    ): BankSyncSummary? {
      val synced = results.filterIsInstance<BankSyncResult.Synced>()
      val single = results.singleOrNull()
      val added = synced.flatMap { it.added }
      val linked =
        added
          .takeIf { it.isNotEmpty() }
          ?.let { sortIds(it) }
          ?.takeIf { it.isNotEmpty() }
          ?.let { LinkedTransactions(it.take(MAX_LINKED), total = it.size) }
      return when {
        results.isEmpty() -> null
        single is BankSyncResult.Synced ->
          Synced(single.name, single.added.size, single.updated.size, linked)
        single is BankSyncResult.Failed -> Failed(single.name)
        else ->
          Several(
            synced = synced.size,
            failed = results.size - synced.size,
            added = added.size,
            updated = synced.sumOf { it.updated.size },
            linked = linked,
          )
      }
    }
  }
}

// The newest of a sync's added transactions, of total that are still there
data class LinkedTransactions(val ids: List<TransactionId>, val total: Int)
