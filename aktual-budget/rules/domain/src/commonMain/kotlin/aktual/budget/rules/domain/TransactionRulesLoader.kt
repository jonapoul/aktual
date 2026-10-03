package aktual.budget.rules.domain

import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.PayeeId
import aktual.core.UuidGenerator
import dev.zacsweers.metro.Inject

/**
 * Loads the open budget's rules, and what they need to know about it, from its database. Only
 * available in the `BudgetScope` graph, since it reads the budget database.
 */
@Inject
class TransactionRulesLoader(
  private val rulesDao: RulesDao,
  private val contextDao: RuleContextDao,
  private val uuidGenerator: UuidGenerator,
) {
  suspend fun load(): LoadedRules {
    val engine =
      RulesEngine(
        rules = rulesDao.getAll().map { it.toTransactionRule() },
        scheduleRules = contextDao.scheduleRules(),
        idMappings = contextDao.idMappings(),
      )
    val context =
      SnapshotRuleContext(
        accounts =
          contextDao.accountsOffBudget().map { (id, offBudget) -> RuleAccount(id, offBudget) },
        payees = contextDao.payees().map { RulePayee(it.id, it.name, it.tombstone) },
        categoryGroups = contextDao.categoryGroups(),
        uuidGenerator = uuidGenerator,
      )
    return LoadedRules(engine, context)
  }
}

/**
 * A snapshot of a budget's rules, ready to run against transactions. Payees that rules rename
 * transactions to but don't exist yet are collected in [createdPayees], and the caller needs to
 * create them when it saves the transactions.
 */
class LoadedRules(val engine: RulesEngine, private val context: SnapshotRuleContext) {
  val createdPayees: Map<PayeeId, String>
    get() = context.createdPayees

  fun run(transaction: RuleTransaction): RuleTransaction = engine.run(transaction, context)

  /**
   * The live payee with this name, ignoring case, or a new one that's added to [createdPayees], as
   * a "set payee_name" action does.
   */
  fun resolvePayee(name: String): PayeeId = context.resolvePayee(name)
}
