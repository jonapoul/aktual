package actual.di.app

import actual.di.core.AppGraph
import actual.di.core.ViewModelGraph
import actual.di.core.ViewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides

@DependencyGraph(ViewModelScope::class)
interface MetroViewModelGraph : ViewModelGraph {
  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Includes appGraph: AppGraph,
      @Provides extras: CreationExtras,
    ): MetroViewModelGraph
  }
}
