package actual.test

import actual.budget.model.BudgetId

fun DatabaseRule.Companion.android(id: BudgetId) = DatabaseRule(androidDriverFactory(id))
