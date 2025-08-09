package actual.app.android

import actual.core.di.ViewModelGraph
import actual.core.di.ViewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Extends
import dev.zacsweers.metro.Provides

@DependencyGraph(ViewModelScope::class)
interface AndroidViewModelGraph : ViewModelGraph {
  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Extends appGraph: AndroidAppGraph,
      @Provides extras: CreationExtras,
    ): AndroidViewModelGraph
  }
}
