package aktual.di

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class AktualViewModelFactory(graph: ViewModelGraph) : MetroViewModelFactory() {
  override val viewModelProviders = graph.viewModelProviders
  override val manualAssistedFactoryProviders = graph.manualAssistedFactoryProviders
  override val assistedFactoryProviders = graph.assistedFactoryProviders
}
