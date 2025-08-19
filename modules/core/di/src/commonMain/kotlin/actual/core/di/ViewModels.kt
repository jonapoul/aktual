package actual.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.MapKey
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provider
import kotlin.reflect.KClass
import kotlin.reflect.cast

sealed interface ViewModelScope

interface ViewModelAssistedFactory

typealias ProviderMap<T> = Map<KClass<out T>, Provider<T>>

interface ViewModelGraph {
  @Multibinds
  val viewModelProviders: ProviderMap<ViewModel>

  @Multibinds
  val assistedFactoryProviders: ProviderMap<ViewModelAssistedFactory>

  fun interface Factory {
    fun create(extras: CreationExtras): ViewModelGraph
  }
}

operator fun <VM : ViewModel> ViewModelGraph.get(type: KClass<VM>): VM =
  viewModelProviders[type]
    ?.invoke()
    ?.let(type::cast)
    ?: error("Unknown view model class $type")

inline fun <reified VM : ViewModel> ViewModelGraph.get(): VM = get(VM::class)

inline fun <reified VM : ViewModel, reified VMAF : ViewModelAssistedFactory> ViewModelGraph.assisted(
  crossinline buildViewModel: VMAF.() -> VM,
): VM {
  val nullableProvider = assistedFactoryProviders[VMAF::class]
    ?.invoke()
    ?.let(VMAF::class::cast)

  val factoryProvider = requireNotNull(nullableProvider) {
    "No factory found for class ${VMAF::class.qualifiedName}"
  }

  return VM::class.cast(factoryProvider.buildViewModel())
}

interface ViewModelGraphProvider : ViewModelProvider.Factory {
  fun buildViewModelGraph(extras: CreationExtras): ViewModelGraph
}

@MapKey
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@MapKey
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AssistedFactoryKey(val value: KClass<out ViewModelAssistedFactory>)
