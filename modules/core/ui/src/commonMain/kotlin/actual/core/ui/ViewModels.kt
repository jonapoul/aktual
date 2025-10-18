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
package actual.core.ui

import actual.core.di.ViewModelAssistedFactory
import actual.core.di.ViewModelGraphProvider
import actual.core.di.assisted
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.reflect.KClass
import kotlin.reflect.cast

val LocalViewModelGraphProvider = staticCompositionLocalOf<ViewModelGraphProvider> {
  error("No ViewModelGraphProvider registered")
}

// Used to fetch ViewModel instances from ViewModelGraph
@Composable
inline fun <reified VM : ViewModel> metroViewModel(
  owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner found" },
  key: String? = null,
  graphProvider: ViewModelGraphProvider = LocalViewModelGraphProvider.current,
): VM = viewModel(
  viewModelStoreOwner = owner,
  key = key,
  factory = graphProvider,
)

/**
 * Used to fetch assisted ViewModel factory instances from ViewModelGraph. Note that there's no
 * compile-time validation that [VM] and [VMAF] types match up to each other (yet?)
 */
@Composable
inline fun <reified VM : ViewModel, reified VMAF : ViewModelAssistedFactory> assistedMetroViewModel(
  owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner found" },
  key: String? = null,
  graphProvider: ViewModelGraphProvider = LocalViewModelGraphProvider.current,
  crossinline buildViewModel: VMAF.() -> VM,
): VM {
  return viewModel(
    viewModelStoreOwner = owner,
    key = key,
    factory = object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val vm = graphProvider
          .buildViewModelGraph(extras)
          .assisted(buildViewModel)
        return modelClass.cast(vm)
      }
    },
  )
}
