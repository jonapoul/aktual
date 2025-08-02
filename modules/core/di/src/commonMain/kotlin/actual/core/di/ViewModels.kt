package actual.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.MapKey
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import kotlin.reflect.KClass

@DependencyGraph(ViewModelScope::class)
interface ViewModelGraph {
  @Multibinds val viewModelProviders: ProviderMap<ViewModel>
  @Multibinds val factoryProviders: ProviderMap<ViewModelFactory>

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Includes appGraph: AppGraph,
      @Provides extras: CreationExtras,
    ): ViewModelGraph
  }

  fun interface Builder : ViewModelProvider.Factory {
    fun build(extras: CreationExtras): ViewModelGraph
  }
}

abstract class ViewModelScope private constructor()

@MapKey
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@MapKey
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelFactoryKey(val value: KClass<*>)
