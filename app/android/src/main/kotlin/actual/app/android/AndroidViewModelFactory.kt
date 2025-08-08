package actual.app.android

import actual.app.di.ActualViewModelFactory
import actual.core.di.ViewModelGraph
import actual.core.di.ViewModelGraphProvider
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metro.createGraphFactory

@Inject
@ContributesBinding(AppScope::class, binding<ViewModelGraphProvider>())
class AndroidViewModelFactory(private val appGraph: AndroidAppGraph) : ActualViewModelFactory {
  override fun buildViewModelGraph(extras: CreationExtras): ViewModelGraph =
    createGraphFactory<AndroidViewModelGraph.Factory>().create(appGraph, extras)
}
