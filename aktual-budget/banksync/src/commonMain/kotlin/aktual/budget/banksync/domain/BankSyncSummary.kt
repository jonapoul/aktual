package aktual.budget.banksync.domain

sealed interface BankSyncSummary {
  data class Synced(val name: String?, val added: Int, val updated: Int) : BankSyncSummary

  data class Failed(val name: String?) : BankSyncSummary

  data class Several(val synced: Int, val failed: Int, val added: Int, val updated: Int) :
    BankSyncSummary

  companion object {
    fun of(results: List<BankSyncResult>): BankSyncSummary? {
      val synced = results.filterIsInstance<BankSyncResult.Synced>()
      val single = results.singleOrNull()
      return when {
        results.isEmpty() -> null
        single is BankSyncResult.Synced ->
          Synced(single.name, single.added.size, single.updated.size)
        single is BankSyncResult.Failed -> Failed(single.name)
        else ->
          Several(
            synced = synced.size,
            failed = results.size - synced.size,
            added = synced.sumOf { it.added.size },
            updated = synced.sumOf { it.updated.size },
          )
      }
    }
  }
}
