package actual.app.desktop

import actual.core.di.AssistedFactoryKey
import actual.core.di.ViewModelAssistedFactory
import actual.core.di.ViewModelScope
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap

// Only exists to make the VM map not empty
@Suppress("unused")
@AssistedInject
class DummyAssistedViewModel(
  @Assisted private val value: Int,
) : ViewModel() {
  @AssistedFactory
  @AssistedFactoryKey(Factory::class)
  @ContributesIntoMap(ViewModelScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    fun create(
      @Assisted value: Int,
    ): DummyAssistedViewModel
  }
}
