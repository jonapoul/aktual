package aktual.budget.schedules.domain

import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.LoadedRules
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.budget.transactions.domain.toNewTransactions
import aktual.core.UuidGenerator
import dev.zacsweers.metro.Inject
import logcat.logcat

// Adds schedule transactions through the rules, as addTransactions() in
// packages/loot-core/src/server/accounts/sync.ts does
@Inject
internal class SchedulePoster(
  private val rulesLoader: TransactionRulesLoader,
  private val transactionWriter: TransactionWriter,
  private val uuidGenerator: UuidGenerator,
) {
  fun batch(): Batch = Batch()

  inner class Batch {
    private var rules: LoadedRules? = null
    private val transactions = mutableListOf<RuleTransaction>()

    // What will be posted after the rules ran, or null if a rule deleted it
    suspend fun add(transaction: RuleTransaction): RuleTransaction? {
      val loaded = rules ?: rulesLoader.load().also { rules = it }
      val result = loaded.run(transaction)
      if (result.tombstone) return null
      transactions += result
      return result
    }

    suspend fun post() {
      if (transactions.isEmpty()) return
      val rows = transactions.flatMap {
        it.toNewTransactions(newId = { uuidGenerator(::TransactionId) })
      }
      val usedPayees = rows.mapNotNull { it.payee }.toSet()
      transactionWriter.write(runTransfers = true) {
        for ((id, name) in rules?.createdPayees.orEmpty()) {
          if (id in usedPayees) insertPayee(name, id = id)
        }
        rows.forEach { insert(it) }
      }
      logcat.i { "Posted schedules: ${transactions.mapNotNull { it.schedule }.distinct()}" }
    }
  }
}
