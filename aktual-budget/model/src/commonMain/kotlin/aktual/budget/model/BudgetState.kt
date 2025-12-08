package aktual.budget.model

enum class BudgetState {
  Local,
  Remote,
  Synced,
  Syncing,
  Detached,
  Broken,
  Unknown,
}
