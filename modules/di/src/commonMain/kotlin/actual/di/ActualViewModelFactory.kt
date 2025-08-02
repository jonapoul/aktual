package actual.di

import actual.core.di.AppGraph
import actual.core.di.ViewModelGraph
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metro.createGraphFactory
import kotlin.reflect.KClass
import kotlin.reflect.cast

@Inject
@ContributesBinding(AppScope::class, binding<ViewModelGraph.Builder>())
class ActualViewModelFactory(private val appGraph: AppGraph) : ViewModelGraph.Builder {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
    build(extras)
      .viewModelProviders[modelClass]
      ?.invoke()
      ?.let(modelClass::cast)
      ?: error("Unknown view model class $modelClass")

  override fun build(extras: CreationExtras): ViewModelGraph =
    createGraphFactory<ViewModelGraph.Factory>().create(appGraph, extras)
}
