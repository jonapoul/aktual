package actual.app.android

import actual.core.di.ViewModelGraph
import actual.core.di.ViewModelGraphProvider
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
class AndroidViewModelFactory(private val appGraph: AndroidAppGraph) : ViewModelGraphProvider {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
    buildViewModelGraph(extras)
      .viewModelProviders[modelClass]
      ?.invoke()
      ?.let(modelClass::cast)
      ?: error("Unknown view model class $modelClass")

  override fun buildViewModelGraph(extras: CreationExtras): ViewModelGraph =
    createGraphFactory<AndroidViewModelGraph.Factory>().create(appGraph, extras)
}
