package aktual.budget.banksync.domain

import aktual.budget.model.AccountSyncSource
import aktual.budget.model.AccountSyncSource.Companion.PluggyAi
import aktual.budget.model.AccountSyncSource.Companion.SimpleFin

// Upstream lets API keys sync only these, using credentials set up for the budget itself
val AccountSyncSource.supportsApiKeys: Boolean
  get() = this == SimpleFin || this == PluggyAi
