package actual.app.di

import actual.core.di.AppGraph
import actual.core.di.ViewModelGraphProvider
import actual.core.di.get
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.reflect.KClass

@Inject
@ContributesBinding(AppScope::class)
class ViewModelFactory(private val appGraph: AppGraph) : ViewModelGraphProvider {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
    buildViewModelGraph(extras)[modelClass]

  override fun buildViewModelGraph(extras: CreationExtras) = appGraph.create(extras)
}
