package aktual.di

import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

// BudgetGraph hangs off AppGraph rather than LoggedInGraph, so it can't see the server-chosen or
// logged-in view models. Merge those of every open level instead, with deeper levels taking
// priority
internal class RunLevelViewModelFactory(levels: List<AktualGraph>) : MetroViewModelFactory() {
  init {
    require(levels.isNotEmpty()) { "No run levels to build view models from" }
  }

  override val viewModelProviders = levels.map { it.viewModelProviders }.reduce { a, b -> a + b }

  override val manualAssistedFactoryProviders =
    levels.map { it.manualAssistedFactoryProviders }.reduce { a, b -> a + b }

  override val assistedFactoryProviders =
    levels.map { it.assistedFactoryProviders }.reduce { a, b -> a + b }
}
