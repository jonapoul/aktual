package actual.di.app

import actual.di.core.AppGraph
import actual.di.core.ViewModelGraph
import actual.di.core.ViewModelGraphProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import kotlin.reflect.KClass
import kotlin.reflect.cast

@Inject
@ContributesBinding(AppScope::class)
class ActualViewModelFactory(private val appGraph: AppGraph) : ViewModelGraphProvider {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
    buildViewModelGraph(extras)
      .viewModelProviders[modelClass]
      ?.invoke()
      ?.let(modelClass::cast)
      ?: error("Unknown view model class $modelClass")

  override fun buildViewModelGraph(extras: CreationExtras): ViewModelGraph =
    createGraphFactory<MetroViewModelGraph.Factory>().create(appGraph, extras)
}
