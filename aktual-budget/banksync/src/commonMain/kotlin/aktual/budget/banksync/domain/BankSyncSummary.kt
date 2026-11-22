package aktual.budget.banksync.domain

import aktual.budget.model.TransactionId

sealed interface BankSyncSummary {
  // The transactions the sync added, for the snackbar to link to
  val added: List<TransactionId>

  data class Synced(
    val name: String?,
    override val added: List<TransactionId>,
    val updated: Int,
  ) : BankSyncSummary

  data class Failed(val name: String?) : BankSyncSummary {
    override val added: List<TransactionId>
      get() = emptyList()
  }

  data class Several(
    val synced: Int,
    val failed: Int,
    override val added: List<TransactionId>,
    val updated: Int,
  ) : BankSyncSummary

  companion object {
    fun of(results: List<BankSyncResult>): BankSyncSummary? {
      val synced = results.filterIsInstance<BankSyncResult.Synced>()
      val single = results.singleOrNull()
      return when {
        results.isEmpty() -> null
        single is BankSyncResult.Synced -> Synced(single.name, single.added, single.updated.size)
        single is BankSyncResult.Failed -> Failed(single.name)
        else ->
          Several(
            synced = synced.size,
            failed = results.size - synced.size,
            added = synced.flatMap { it.added },
            updated = synced.sumOf { it.updated.size },
          )
      }
    }
  }
}
