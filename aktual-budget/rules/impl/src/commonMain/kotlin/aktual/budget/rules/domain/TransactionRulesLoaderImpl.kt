package aktual.budget.rules.domain

import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.PayeeId
import aktual.core.utils.UuidGenerator
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(BudgetScope::class)
class TransactionRulesLoaderImpl(
  private val rulesDao: RulesDao,
  private val contextDao: RuleContextDao,
  private val uuidGenerator: UuidGenerator,
) : TransactionRulesLoader {
  override suspend fun load(): LoadedRulesImpl {
    val engine =
      RulesEngine(
        rules = rulesDao.getAll().map { it.toTransactionRule() },
        scheduleRules = contextDao.scheduleRules(),
        completedScheduleRules = contextDao.completedScheduleRules(),
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
    return LoadedRulesImpl(engine, context)
  }
}

class LoadedRulesImpl(val engine: RulesEngine, private val context: SnapshotRuleContext) :
  LoadedRules {
  override val createdPayees: Map<PayeeId, String>
    get() = context.createdPayees

  override fun run(transaction: RuleTransaction): RuleTransaction = engine.run(transaction, context)

  fun run(actions: RuleActions, transaction: RuleTransaction): RuleTransaction =
    actions.run(transaction, context)

  override fun resolvePayee(name: String): PayeeId = context.resolvePayee(name)
}
