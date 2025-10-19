/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.core.di

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
