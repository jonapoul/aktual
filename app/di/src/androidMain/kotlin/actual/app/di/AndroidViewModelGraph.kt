package actual.app.di

import actual.core.di.ViewModelGraph
import actual.core.di.ViewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(ViewModelScope::class)
interface AndroidViewModelGraph : ViewModelGraph {
  @GraphExtension.Factory
  fun interface Factory : ViewModelGraph.Factory {
    override fun create(
      @Provides extras: CreationExtras,
    ): AndroidViewModelGraph
  }
}
