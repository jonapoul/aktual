@file:OptIn(ExperimentalForInheritanceCoroutinesApi::class)

package aktual.test

import aktual.budget.model.DbMetadata
import aktual.budget.prefs.BudgetLocalPreferences
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

class TestBudgetLocalPreferences private constructor(delegate: MutableStateFlow<DbMetadata>) :
  BudgetLocalPreferences, MutableStateFlow<DbMetadata> by delegate {
  constructor(initial: DbMetadata = DbMetadata()) : this(MutableStateFlow(initial))
}
