package actual.core.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Multibinds

interface ViewModelGraph {
  @Multibinds
  val viewModelProviders: ProviderMap<ViewModel>

  @Multibinds
  val assistedFactoryProviders: ProviderMap<ViewModelAssistedFactory>
}
