package aktual.budget.rules.domain

import aktual.budget.db.Rules

fun Rules.toTransactionRule(): TransactionRule =
  TransactionRule(
    id = id,
    conditions = conditions.orEmpty(),
    actions = actions.orEmpty(),
    stage = stage,
    conditionsOp = conditions_op ?: And,
  )
